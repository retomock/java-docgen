package ch.retomock.docgen.output;

import ch.retomock.docgen.domain.ServiceMethod;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlOutputFormat implements OutputFormat {

  private final FileWriter out;

  public HtmlOutputFormat(FileWriter out) throws IOException {
    this.out = out;
    out.write("<html><body>\n");
  }

  @Override
  public void title(String moduleName) throws IOException {
    out.write("<h1>Service: " + moduleName + "</h1>");
  }

  @Override
  public void startTable() throws IOException {
    out.write("<table border='1'>");
  }

  @Override
  public void endTable() throws IOException {
    out.write("</table>");
  }

  @Override
  public void header(String... headers) throws IOException {
    out.write("<tr>");
    for (var header : headers) {
      out.write("<td>");
      out.write(header);
      out.write("</th>");
    }
    out.write("</tr>\n");
  }

  @Override
  public void writeServiceMethod(ServiceMethod serviceMethod) throws IOException {
    out.write("<tr><td><pre>");
    out.write(serviceMethod.getName());
    out.write("</pre></td>");
    out.write("<td><pre>");
    if (serviceMethod.getRequiredPermission() != null) {
      out.write(serviceMethod.getRequiredPermission());
    }
    out.write("</pre></td>");
    out.write("<td><pre><ul>");
    for (var conditionalPermission : serviceMethod.getConditionalPermissions()) {
      out.write("<li>");
      out.write(conditionalPermission);
      out.write("</li>");
    }
    out.write("</ul></pre></td>");
    out.write("<td><pre><ul>");
    for (var grpcServiceCall : serviceMethod.getGrpcServiceCalls()) {
      out.write("<li>");
      out.write(grpcServiceCall);
      out.write("</li>");
    }
    out.write("</ul></pre></td>");
    out.write("<td><pre><ul>");
    for (var dbTable : serviceMethod.getDatabaseTables()) {
      out.write("<li>");
      out.write(dbTable);
      out.write("</li>");
    }
    out.write("</ul></pre></td>");
    out.write("<td>");
    out.write(serviceMethod.getComment());
    out.write("</td></tr>\n");
  }

  @Override
  public void close() throws Exception {
    out.write("</body></html>");
    out.close();
  }
}
