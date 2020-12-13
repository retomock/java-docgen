package ch.retomock.docgen.config;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;

public class DocGenConfigException extends RuntimeException {

  public DocGenConfigException(String message) {
    super(message);
  }

  public DocGenConfigException(Set<ConstraintViolation<DocGenConfig>> configErrors) {
    super("Configuration contains errors: " + configErrors.stream()
        .map(cv -> "'" + cv.getPropertyPath() + "' " + cv.getMessage())
        .collect(Collectors.joining(" and ")));
  }
}
