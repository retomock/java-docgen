package ch.retomock.docgen.domain;

import java.util.Collection;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServiceMethod implements Comparable<ServiceMethod> {

  private final String name;
  private final String requiredPermission;
  private final Collection<String> conditionalPermissions;
  private final Collection<String> grpcServiceCalls;
  private final Collection<String> databaseTables;
  private final String comment;

  @Override
  public int compareTo(ServiceMethod other) {
    return name.compareTo(other.name);
  }
}
