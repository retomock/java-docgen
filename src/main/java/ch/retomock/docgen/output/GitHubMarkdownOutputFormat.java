package ch.retomock.docgen.output;

import ch.retomock.docgen.config.DocGenConfig.Module;
import ch.retomock.docgen.domain.ServiceMethod;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class GitHubMarkdownOutputFormat implements OutputFormat {

  private final FileWriter out;

  public GitHubMarkdownOutputFormat(FileWriter out) throws IOException {
    this.out = out;
    out.write("# Service Documentation\n"
        + "Please note that this documentation was automatically generated from the code. Do not edit it manually.\n\n"
        + "Last update: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(OffsetDateTime.now()) + "\n\n");
  }

  @Override
  public void tableOfContents(Module[] modules) throws IOException {
    if (modules.length > 1) {
      out.write("## Services Index:\n\n");
      for (var module : modules) {
        out.write("- [");
        out.write(module.getName());
        out.write("](#service-");
        out.write(module.getName()
            .replace("/", "")
            .replace("|", "")
            .replace("(", "")
            .replace(")", ""));
        out.write(")\n");
      }
    }
  }

  @Override
  public void title(String moduleName) throws IOException {
    out.write("\n\n## Service: " + moduleName + "\n\n");
  }

  @Override
  public void startTable() {
    // no op
  }

  @Override
  public void endTable() {
    // np op
  }

  @Override
  public void header(String... headers) throws IOException {
    out.write("| ");
    for (var header : headers) {
      out.write(header);
      out.write(" |");
    }
    out.write("\n| ");
    for (var header : headers) {
      out.write(" -- |");
    }
    out.write("\n");
  }

  @Override
  public void serviceMethod(ServiceMethod serviceMethod) throws IOException {
    out.write("| <a name=\"");
    out.write(serviceMethod.getName());
    out.write("\"></a>[");
    out.write(serviceMethod.getName());
    out.write("](");
    out.write(serviceMethod.getSourceLink());
    out.write(") | ");
    if (serviceMethod.getRequiredPermission() != null) {
      out.write(serviceMethod.getRequiredPermission());
    }
    out.write(" | ");
    out.write("<ul>");
    for (var conditionalPermission : serviceMethod.getConditionalPermissions()) {
      out.write("<li>");
      out.write(conditionalPermission);
      out.write("</li>");
    }
    out.write("</ul> | ");
    out.write("<ul>");
    for (var grpcServiceCall : serviceMethod.getGrpcServiceCalls()) {
      out.write("<li>[");
      out.write(grpcServiceCall);
      out.write("](#user-content-");
      out.write(grpcServiceCall.toLowerCase());
      out.write(")</li>");
    }
    out.write("</ul> | ");
    out.write("<ul>");
    for (var dbTable : serviceMethod.getDatabaseTables()) {
      out.write("<li>");
      out.write(dbTable);
      out.write("</li>");
    }
    out.write("</ul> | ");
    out.write(serviceMethod.getComment()
        .replace("<pre>", "")
        .replace("</pre>", "")
        .replace("\n", "<br>"));
    out.write(" |\n");
  }

  @Override
  public void close() throws Exception {
    out.write("\nGenerated by [java-docgen](https://github.com/retomock/java-docgen)");
    out.close();
  }
}
