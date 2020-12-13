package ch.retomock.docgen.output;

import ch.retomock.docgen.config.DocGenConfigException;
import ch.retomock.docgen.domain.ServiceMethod;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public interface OutputFormat extends AutoCloseable {

  void title(String moduleName) throws IOException;

  void startTable() throws IOException;

  void endTable() throws IOException;

  void header(String... headers) throws IOException;

  void writeServiceMethod(ServiceMethod serviceMethod) throws IOException;

  static OutputFormat getOutputFormat(File outputFile) throws IOException {
    if (outputFile.getName().endsWith(".md")) {
      return new GitHubMarkdownOutputFormat(new FileWriter(outputFile));
    }
    if (outputFile.getName().endsWith(".html")) {
      return new HtmlOutputFormat(new FileWriter(outputFile));
    }
    throw new DocGenConfigException("Unsupported file extension for output file: " + outputFile.getName());
  }
}
