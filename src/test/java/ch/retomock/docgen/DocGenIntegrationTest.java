package ch.retomock.docgen;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.retomock.docgen.config.DocGenConfig;
import ch.retomock.docgen.domain.ServiceMethod;
import ch.retomock.docgen.output.OutputFormat;
import ch.retomock.docgen.util.SpringBootJarLoader;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocGenIntegrationTest {

  private DocGenConfig docGenConfig;
  private DocGen docGen;

  @BeforeEach
  private void setUp() throws Exception {
    docGenConfig = DocGenConfig.loadConfig(new String[]{
        DocGenIntegrationTest.class.getResource("/integration-test-config.json").getFile()});
    var jarLoader = new SpringBootJarLoader(docGenConfig.getJarRepositoryPath(), docGenConfig.getJarsToAnalyze());
    docGen = new DocGen(docGenConfig, jarLoader);
  }

  @Test
  void shouldProcessMethodWithConditionalPermission() throws Exception {
    // given
    var outputFormat = mock(OutputFormat.class);

    // when
    docGen.processModule(docGenConfig.getModules()[0], outputFormat);

    // then
    verify(outputFormat).title("example-service");
    verify(outputFormat).serviceMethod(ServiceMethod.builder()
        .name("ExampleService.doSomething")
        .requiredPermission("")
        .conditionalPermissions(Set.of("view:data"))
        .grpcServiceCalls(List.of())
        .databaseTables(Set.of())
        .comment("<pre>\n"
            + "This is just an example service method.\n"
            + "</pre>")
        .build());
  }

  @Test
  void shouldProcessMethodWithRequiredPermissionAndServiceCall() throws Exception {
    // given
    var outputFormat = mock(OutputFormat.class);

    // when
    docGen.processModule(docGenConfig.getModules()[0], outputFormat);

    // then
    verify(outputFormat).title("example-service");
    verify(outputFormat).serviceMethod(ServiceMethod.builder()
        .name("ExampleService.doSomethingElse")
        .requiredPermission("some-permission")
        .conditionalPermissions(Set.of())
        .grpcServiceCalls(List.of("ExampleService.doSomething"))
        .databaseTables(Set.of())
        .comment("<pre>\n"
            + "This is another service method.\n"
            + "</pre>")
        .build());
  }

  @Test
  void shouldProcessMethodWithDatabaseAccess() throws Exception {
    // given
    var outputFormat = mock(OutputFormat.class);

    // when
    docGen.processModule(docGenConfig.getModules()[0], outputFormat);

    // then
    verify(outputFormat).title("example-service");
    verify(outputFormat).serviceMethod(ServiceMethod.builder()
        .name("ExampleService.doSomethingWithDatabase")
        .requiredPermission("")
        .conditionalPermissions(Set.of())
        .grpcServiceCalls(List.of())
        .databaseTables(Set.of("EXAMPLE"))
        .comment("<pre>\n"
            + "A service method that accessed the database.\n"
            + "</pre>")
        .build());
  }
}
