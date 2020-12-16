package ch.retomock.docgen.scanner.collector;

import ch.retomock.docgen.util.SpringBootJarLoader;
import java.lang.reflect.Method;
import java.util.TreeSet;
import org.jooq.Table;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;

public class JooqTableCollector implements Collector {

  private final SpringBootJarLoader classLoader;
  private final Method getAliasedTableMethod;
  private final TreeSet<String> tables = new TreeSet<>();

  public JooqTableCollector(SpringBootJarLoader classLoader) throws NoSuchMethodException {
    this.classLoader = classLoader;

    this.getAliasedTableMethod = TableImpl.class.getDeclaredMethod("getAliasedTable");
    this.getAliasedTableMethod.setAccessible(true);
  }

  @Override
  public void referencedMethod(
      String className,
      String methodName,
      String signature,
      String lastConstant,
      String lastFieldOwner,
      String lastField,
      int lastVar
  ) {
    if (className.startsWith("org.jooq") && methodName.equals("from")) {
      referencedTable(lastFieldOwner, lastField, lastVar);
    } else if (className.startsWith("org.jooq") && methodName.toLowerCase().contains("join")) {
      referencedTable(lastFieldOwner, lastField, lastVar);
    } else if (className.toLowerCase().contains("generated.table")) {
      try {
        var clazz = classLoader.loadClass(className);
        if (TableImpl.class.isAssignableFrom(clazz)) {
          var table = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
          var method = clazz.getMethod("getName", new Class[0]);
          method.setAccessible(true);
          var tableName = (String) method.invoke(table);
          tables.add(tableName);
        } else if (TableRecordImpl.class.isAssignableFrom(clazz)) {
          var record = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
          var method = TableRecordImpl.class.getMethod("getTable", new Class[0]);
          var tableName = ((Table) method.invoke(record)).getName();
          tables.add(tableName);
        }
      } catch (Exception e) {
        System.err.println("WARNING: Unable to determine table name: " + e);
        tables.add("&lt;unknown table&gt;");
      }
    }
  }

  private void referencedTable(String lastFieldOwner, String lastField, int lastVar) {
    if (lastVar >= 0) {
      System.err.println("WARNING: Table access through local variable not supported yet");
      tables.add("&lt;dynamic table&gt;");
    } else {
      try {
        var field = classLoader.loadClass(lastFieldOwner.replace("/", ".")).getDeclaredField(lastField);
        field.setAccessible(true);
        var table = (TableImpl) field.get(null); // public static field
        var aliasedTable = (Table) getAliasedTableMethod.invoke(table);

        tables.add(aliasedTable != null ? aliasedTable.getName() : table.getName());
      } catch (Exception e) {
        System.err.println("WARNING: Unable to determine table name: " + e);
        tables.add("&lt;unknown table&gt;");
      }
    }
  }

  public TreeSet<String> getTables() {
    return tables;
  }
}
