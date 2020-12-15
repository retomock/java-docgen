package ch.retomock.docgen.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.retomock.docgen.config.DocGenConfig.Module;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class DocGenConfigTest {

  @Test
  void shouldLoadConfig() throws IOException {
    // given
    var configFile = DocGenConfigTest.class.getResource("/valid-config.json").getFile();

    // when
    var config = DocGenConfig.loadConfig(new String[]{configFile});

    // then
    assertThat(config.getProjectRootDirectory()).isEqualTo("projectRoot");
    assertThat(config.getJavaDocSourceDirectories()).containsExactly("javaDocDirectory");
    assertThat(config.getBasePackage()).isEqualTo("ch.retomock");
    assertThat(config.getModules()).containsExactly(
        new Module("service1", "src/main/java", "ch.retomock.docgen.example.service1"),
        new Module("service2", "src/main/java", "ch.retomock.docgen.example.service2"));
    assertThat(config.getJarRepositoryPath()).isEqualTo("jarRepoPath");
    assertThat(config.getJarsToAnalyze()).containsExactly("1.jar", "2.jar");
    assertThat(config.getOutputFile()).isEqualTo("out.html");
  }

  @Test
  void shouldValidateConfig() {
    // given
    var configFile = DocGenConfigTest.class.getResource("/invalid-config.json").getFile();

    // when
    assertThatThrownBy(() -> DocGenConfig.loadConfig(new String[]{configFile}))
        .isInstanceOf(DocGenConfigException.class)
        .hasMessageStartingWith("Configuration contains errors:")
        .hasMessageContaining("'outputFile' must not be blank")
        .hasMessageContaining("'modules' must not be empty");
  }
}
