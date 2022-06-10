package ch.retomock.docgen.scanner;

import ch.retomock.docgen.domain.MethodReference;
import ch.retomock.docgen.scanner.collector.AggregateCollector;
import ch.retomock.docgen.scanner.visitor.DocGenClassVisitor;
import ch.retomock.docgen.util.SpringBootJarLoader;
import java.util.ArrayDeque;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassReader;

@RequiredArgsConstructor
public class ClassScanner {

  private final SpringBootJarLoader classLoader;
  private final String basePackage;

  public void walkCallTree(
      String className,
      String methodName,
      String signature,
      AggregateCollector collector,
      ArrayDeque<MethodReference> callStack
  ) throws Exception {
    var childCollector = findReferencedMethods(className, methodName, signature);
    collector.sourceFile(childCollector.getSourceFile());
    collector.lineNumber(childCollector.getLineNumber());
    for (var referencedMethod : childCollector.getMethodCollector().getReferencedMethods()) {
      if (referencedMethod.getClassName().contains("Grpc$")) {
        collector.getMethodCollector().getReferencedMethods().add(referencedMethod);
      } else {
        if (callStack.contains(referencedMethod)) {
          System.err.println("WARNING: Reccursion detected: " + referencedMethod);
        } else if (!referencedMethod.getClassName().contains("Builder")) {
          callStack.push(referencedMethod);
          walkCallTree(
              referencedMethod.getClassName(),
              referencedMethod.getMethodName(),
              referencedMethod.getSignature(),
              collector,
              callStack);
          callStack.pop();
        }
      }
    }
    collector.getPermissionCollector().getPermissions().addAll(childCollector.getPermissionCollector().getPermissions());
    collector.getJooqTableCollector().getTables().addAll(childCollector.getJooqTableCollector().getTables());
  }

  private AggregateCollector findReferencedMethods(
      String className,
      String methodName,
      String expectedSignature
  ) throws Exception {
    var classReader = new ClassReader(classLoader.getClassAsStream(className));
    var collector = new AggregateCollector(classLoader, basePackage);
    var classVisitor = new DocGenClassVisitor(methodName, expectedSignature, collector);
    try {
      classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
    } catch (Exception e) {
      System.err.println("WARNING: Error reading " + className + ": " + e.getMessage());
    }
    if (!classVisitor.isMethodFound()) {
      try {
        var superClass = classLoader.loadClass(className).getSuperclass().getName();
        if (superClass.equals("java.lang.Object")) {
          System.err.println("WARNING: Could not find " + className + "." + methodName
              + ". Make sure to put all necessary JARs onto the scan path.");
          return collector;
        }
        return findReferencedMethods(superClass, methodName, expectedSignature);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return collector;
  }
}