package ch.retomock.docgen.domain;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class MethodReference implements Comparable<MethodReference> {

  String className;
  String methodName;
  String signature;

  public String toString() {
    return className + "." + methodName;
  }

  @Override
  public int compareTo(MethodReference other) {
    return other.toString().compareTo(toString());
  }
}