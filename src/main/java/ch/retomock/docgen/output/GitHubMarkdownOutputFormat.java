package ch.retomock.docgen.output;

import ch.retomock.docgen.domain.ServiceMethod;
import java.io.FileWriter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitHubMarkdownOutputFormat implements OutputFormat {

  private final FileWriter out;

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
  public void writeServiceMethod(ServiceMethod serviceMethod) throws IOException {
    out.write("| ");
    out.write(serviceMethod.getName());
    out.write(" | ");
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
      out.write("<li>");
      out.write(grpcServiceCall);
      out.write("</li>");
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
    out.close();
  }
}
