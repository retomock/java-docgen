package ch.retomock.docgen.util;

import ch.retomock.docgen.config.DocGenConfigException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.boot.loader.jar.JarFile;

public class SpringBootJarLoader extends JarLauncher {

  private final String jarRepositoryPath;
  private final String[] jarsToAnalyze;
  private final ClassLoader classLoader;

  public SpringBootJarLoader(String jarRepositoryPath, String[] jarsToAnalyze) throws Exception {
    this.jarRepositoryPath = jarRepositoryPath;
    this.jarsToAnalyze = jarsToAnalyze;
    JarFile.registerUrlProtocolHandler();
    this.classLoader = createClassLoader(getClassPathArchives());
  }

  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return classLoader.loadClass(className);
  }

  public InputStream getClassAsStream(String className) throws ClassNotFoundException {
    var classFile = className.replace('.', '/') + ".class";
    var is = classLoader.getResourceAsStream(classFile);
    if (is == null) {
      is = classLoader.getResourceAsStream("BOOT-INF/classes/" + classFile);
    }
    if (is == null) {
      throw new ClassNotFoundException(className);
    }
    return is;
  }

  @Override
  protected List<Archive> getClassPathArchives() throws Exception {
    List<Archive> archives = new ArrayList();
    for (var jarPath : jarsToAnalyze) {
      var jarFile = new File(new File(jarRepositoryPath), jarPath);
      if (!jarFile.exists()) {
        throw new DocGenConfigException("JAR file not found: " + jarFile.getAbsolutePath());
      }
      var archive = new JarFileArchive(jarFile);
      archives.add(archive);
      archive.getNestedArchives(null, this::isNestedArchive).forEachRemaining(archives::add);
    }
    return archives;
  }
}
