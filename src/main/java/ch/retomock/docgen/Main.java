package ch.retomock.docgen;

import ch.retomock.docgen.config.DocGenConfig;
import ch.retomock.docgen.config.DocGenConfigException;
import ch.retomock.docgen.output.OutputFormat;
import ch.retomock.docgen.util.SpringBootJarLoader;
import java.io.File;

public class Main {

  public static void main(String[] args) {
    try {
      var config = DocGenConfig.loadConfig(args);
      var classLoader = new SpringBootJarLoader(config.getJarRepositoryPath(), config.getJarsToAnalyze());
      var docGen = new DocGen(config, classLoader);

      var outputFile = new File(config.getOutputFile());
      try (var outputFormat = OutputFormat.getOutputFormat(outputFile)) {
        outputFormat.tableOfContents(config.getModules());
        for (var module : config.getModules()) {
          docGen.processModule(module, outputFormat);
        }
        System.out.println("All done. Output written to: " + outputFile.getAbsolutePath());
      }
    } catch (DocGenConfigException e) {
      System.err.println(e.getMessage());
    } catch (ClassNotFoundException e) {
      System.err.println("Class not found. Please check your config: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Sorry, but something went wrong");
      e.printStackTrace();
    }
  }
}
