package ch.retomock.docgen.scanner.collector;

public interface Collector {

  void referencedMethod(
      String className,
      String methodName,
      String signature,
      String lastConstant,
      String lastFieldOwner,
      String lastField,
      int lastVar);

}
