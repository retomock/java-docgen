package ch.retomock.docgen.scanner.visitor;

import ch.retomock.docgen.scanner.collector.AggregateCollector;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class DocGenMethodVisitor extends MethodVisitor {

  private final AggregateCollector aggregateCollector;
  private String lastConstant;
  private String lastFieldOwner;
  private String lastField;
  private int lastVar = -1;

  public DocGenMethodVisitor(AggregateCollector aggregateCollector) {
    super(Opcodes.ASM7);
    this.aggregateCollector = aggregateCollector;
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
    this.lastConstant = null;
    this.lastFieldOwner = owner;
    this.lastField = name;
    this.lastVar = -1;
  }

  @Override
  public void visitLdcInsn(Object value) {
    this.lastConstant = value.toString();
    this.lastFieldOwner = null;
    this.lastField = null;
    this.lastVar = -1;
  }

  @Override
  public void visitVarInsn(int opcode, int var) {
    this.lastConstant = null;
    this.lastFieldOwner = null;
    this.lastField = null;
    this.lastVar = var;
  }

  @Override
  public void visitMethodInsn(
      final int opcode,
      final String owner,
      final String name,
      final String descriptor,
      final boolean isInterface) {
    referencedMethod(owner, name, descriptor);
  }

  @Override
  public void visitInvokeDynamicInsn(
      final String name,
      final String descriptor,
      final Handle bootstrapMethodHandle,
      final Object... bootstrapMethodArguments) {

    if (bootstrapMethodArguments.length > 1) {
      var target = (Handle) bootstrapMethodArguments[1];
      referencedMethod(target.getOwner(), target.getName(), target.getDesc());
    }
  }

  @Override
  public void visitLineNumber(int line, Label start) {
    aggregateCollector.lineNumber(line);
  }

  void referencedMethod(String owner, String methodName, String signature) {
    var className = Type.getObjectType(owner).getClassName();
    aggregateCollector.referencedMethod(className, methodName, signature, lastConstant, lastFieldOwner, lastField, lastVar);
  }
}
