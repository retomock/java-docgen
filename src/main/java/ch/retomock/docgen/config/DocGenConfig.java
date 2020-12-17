package ch.retomock.docgen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DocGenConfig {

  /**
   * Root directory of the Java project to be analyzed
   */
  @NotBlank
  private String projectRootDirectory;

  /**
   * Java source code directories to be scanner for JavaDoc comments (relative to <code>projectRootDirectory</code>)
   */
  @NotEmpty
  private String[] javaDocSourceDirectories;

  /**
   * Base package of your company / project
   */
  @NotEmpty
  private String basePackage;

  /**
   * Modules to be analyzed
   */
  @NotEmpty
  @Valid
  private Module[] modules;

  /**
   * Base path where the JAR files are located. E.g. ~/.m2/repository
   */
  @NotBlank
  private String jarRepositoryPath;

  /**
   * JARs to Analyze (relative to <code>jarRepositoryPath</code>)
   */
  @NotEmpty
  private String[] jarsToAnalyze;

  /**
   * Where to write the output to (supported formats: .html or .md)
   */
  @NotBlank
  private String outputFile;

  private String sourceControlBaseUrl;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Module {

    /**
     * Name of the module (used for display purposes only)
     */
    @NotBlank
    private String name;

    /**
     * Folder containing the Java source code files (relative to <code>projectRootDirectory</code>)
     */
    @NotBlank
    private String sourceCodeFolder;

    /**
     * Java package containing the classes that extend the generated gRPC <code>*ImplBase</code>
     */
    @NotBlank
    private String servicePackage;
  }

  public static DocGenConfig loadConfig(String[] args) throws IOException {
    if (args.length != 1) {
      throw new DocGenConfigException("The document generator expects a single argument pointing to the JSON config file");
    }
    var configFile = new File(args[0]);
    if (!configFile.exists()) {
      throw new DocGenConfigException("Config file not found: " + configFile.getAbsolutePath());
    }
    var config = new ObjectMapper().readValue(configFile, DocGenConfig.class);
    var validator = Validation.buildDefaultValidatorFactory().getValidator();
    var configErrors = validator.validate(config);
    if (!configErrors.isEmpty()) {
      throw new DocGenConfigException(configErrors);
    }
    return config;
  }
}
