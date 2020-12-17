package ch.retomock.docgen.scanner.collector;

import ch.retomock.docgen.util.SpringBootJarLoader;
import lombok.Getter;

@Getter
public class AggregateCollector implements Collector {

  private final MethodCollector methodCollector;
  private final PermissionCollector permissionCollector;
  private final JooqTableCollector jooqTableCollector;

  private String sourceFile;
  private int lineNumber = -1;

  public AggregateCollector(SpringBootJarLoader classLoader, String basePackage) throws NoSuchMethodException {
    this.methodCollector = new MethodCollector(basePackage);
    this.permissionCollector = new PermissionCollector();
    this.jooqTableCollector = new JooqTableCollector(classLoader);
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
    methodCollector.referencedMethod(className, methodName, signature, lastConstant, lastFieldOwner, lastField, lastVar);
    permissionCollector.referencedMethod(className, methodName, signature, lastConstant, lastFieldOwner, lastField, lastVar);
    jooqTableCollector.referencedMethod(className, methodName, signature, lastConstant, lastFieldOwner, lastField, lastVar);
  }

  public void sourceFile(String sourceFile) {
    if (this.sourceFile == null) {
      this.sourceFile = sourceFile;
    }
  }

  public void lineNumber(int lineNumber) {
    if (this.lineNumber < 0) {
      this.lineNumber = lineNumber;
    }
  }
}
