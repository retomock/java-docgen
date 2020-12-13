package ch.retomock.docgen.scanner.collector;

import java.util.TreeSet;

public class PermissionCollector implements Collector {

  private final TreeSet<String> permissions = new TreeSet<>();

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
    if (methodName.equals("hasPermission")) { // TODO make configurable
      if (lastConstant == null) {
        System.err.println("WARNING: hasPermission() called with dynamic value");
      }
      permissions.add(lastConstant != null ? lastConstant : "&lt;dynamic value&gt;");
    }
  }

  public TreeSet<String> getPermissions() {
    return permissions;
  }
}
