package ch.retomock.docgen.scanner.visitor;

import ch.retomock.docgen.scanner.collector.AggregateCollector;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DocGenClassVisitor extends ClassVisitor {

  private final String methodName;
  private final String expectedSignature;
  private final AggregateCollector collector;
  private boolean methodFound = false;

  public DocGenClassVisitor(String methodName, String expectedSignature,
      AggregateCollector collector) {
    super(Opcodes.ASM7);
    this.methodName = methodName;
    this.expectedSignature = expectedSignature;
    this.collector = collector;
  }

  @Override
  public MethodVisitor visitMethod(
      final int access,
      final String name,
      final String descriptor,
      final String signature,
      final String[] exceptions) {
    if (name.equals(methodName) && (expectedSignature.equals(descriptor) || expectedSignature.equals(signature))) {
      methodFound = true;
      return new DocGenMethodVisitor(collector);
    }
    return null;
  }

  @Override
  public void visitSource(String source, String debug) {
    collector.sourceFile(source);
  }

  public boolean isMethodFound() {
    return methodFound;
  }
}
