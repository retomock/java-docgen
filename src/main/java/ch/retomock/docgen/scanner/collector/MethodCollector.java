package ch.retomock.docgen.scanner.collector;

import ch.retomock.docgen.domain.MethodReference;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MethodCollector implements Collector {

  private final Set<String> METHOD_BLACKLIST = Set.of("newStub", "newBlockingStub", "newFutureStub", "withDeadline", "apply");

  private final String basePackage;
  private final TreeSet<MethodReference> referencedMethods = new TreeSet<>();

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
    if (className.startsWith(basePackage) && !METHOD_BLACKLIST.contains(methodName)) {
      referencedMethods.add(new MethodReference(className, methodName, signature));
    }
  }

  public TreeSet<MethodReference> getReferencedMethods() {
    return referencedMethods;
  }
}
