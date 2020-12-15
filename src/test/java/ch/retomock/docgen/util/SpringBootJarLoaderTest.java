package ch.retomock.docgen.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpringBootJarLoaderTest {

  public static final String MAVEN_REPO = ".m2/repository";
  public static final String EXAMPLE_SERVICE_JAR = "ch/retomock/docgen/example/example-service/1.0-SNAPSHOT/example-service-1.0-SNAPSHOT.jar";
  public static final String EXAMPLE_SERVICE_CLASS = "ch.retomock.docgen.example.service.ExampleService";
  public static final byte[] JAVA_CLASS_HEADER = {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe}; // CAFEBABE

  private SpringBootJarLoader loader;

  @BeforeEach
  void setUp() throws Exception {
    var userHome = new File(System.getProperty("user.home"));
    var mavenRepo = new File(userHome, MAVEN_REPO);
    if (!mavenRepo.exists()) {
      throw new FileNotFoundException("Maven repo not found: " + mavenRepo.getAbsolutePath());
    }
    var exampleJar = new File(mavenRepo, EXAMPLE_SERVICE_JAR);
    if (!exampleJar.exists()) {
      throw new FileNotFoundException("Example service JAR not found. Please 'mvn install' the example-service: " + exampleJar);
    }
    loader = new SpringBootJarLoader(mavenRepo.getAbsolutePath(), new String[]{EXAMPLE_SERVICE_JAR});
  }

  @Test
  void shouldLoadClass() throws ClassNotFoundException {
    // when
    var clazz = loader.loadClass(EXAMPLE_SERVICE_CLASS);
    // then
    assertThat(clazz).isNotNull();
    assertThat(clazz.getName()).isEqualTo(EXAMPLE_SERVICE_CLASS);
  }

  @Test
  void shouldGetClassAsStream() throws ClassNotFoundException, IOException {
    // when
    var inputStream = loader.getClassAsStream(EXAMPLE_SERVICE_CLASS);
    // then
    assertThat(inputStream).isNotNull();
    assertThat(inputStream.readNBytes(4)).isEqualTo(JAVA_CLASS_HEADER);
  }
}
