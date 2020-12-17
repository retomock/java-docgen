package ch.retomock.docgen;

import ch.retomock.docgen.config.DocGenConfig;
import ch.retomock.docgen.config.DocGenConfig.Module;
import ch.retomock.docgen.config.DocGenConfigException;
import ch.retomock.docgen.domain.ServiceMethod;
import ch.retomock.docgen.output.OutputFormat;
import ch.retomock.docgen.scanner.ClassScanner;
import ch.retomock.docgen.scanner.JavaDocSourceScanner;
import ch.retomock.docgen.scanner.collector.AggregateCollector;
import ch.retomock.docgen.util.SpringBootJarLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

public class DocGen {

  private final DocGenConfig config;
  private final SpringBootJarLoader classLoader;
  private final ClassScanner classScanner;
  private final Map<String, String> javaDocComments;
  private final Method getGenericSignatureMethod;

  public DocGen(DocGenConfig config, SpringBootJarLoader classLoader) throws Exception {
    this.config = config;
    this.classLoader = classLoader;
    this.classScanner = new ClassScanner(classLoader, config.getBasePackage());
    this.javaDocComments = JavaDocSourceScanner
        .collectJavaDoc(config.getProjectRootDirectory(), config.getJavaDocSourceDirectories());

    getGenericSignatureMethod = Method.class.getDeclaredMethod("getGenericSignature");
    getGenericSignatureMethod.setAccessible(true);
  }

  public void processModule(Module module, OutputFormat outputFormat) throws Exception {
    var sourceCodeFolder = new File(new File(config.getProjectRootDirectory(), module.getSourceCodeFolder()),
        module.getServicePackage().replace(".", "/"));
    if (!sourceCodeFolder.exists()) {
      throw new DocGenConfigException("Source code not found: " + sourceCodeFolder.getAbsolutePath());
    }

    var serviceMethods = new TreeSet<ServiceMethod>();
    for (var file : sourceCodeFolder.listFiles()) {
      var className = module.getServicePackage() + "." + file.getName().replace(".java", "");
      System.out.println("Processing: " + className);
      var clazz = classLoader.loadClass(className);
      for (var method : clazz.getDeclaredMethods()) {
        var javaDocKey = method.getDeclaringClass().getSuperclass().getSimpleName() + "." + method.getName();
        Optional.ofNullable(javaDocComments.get(javaDocKey)).ifPresent(comment -> {
          try {
            serviceMethods.add(processServiceMethod(getGenericSignatureMethod, clazz, method, comment, module));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
      }
    }
    outputFormat.title(module.getName());
    outputFormat.startTable();
    outputFormat.header(
        "Service Method",
        "Required Permission",
        "Conditional on Permission",
        "gRPC Service Calls",
        "DB Tables accessed",
        "Description");
    for (var serviceMethod : serviceMethods) {
      outputFormat.serviceMethod(serviceMethod);
    }
    outputFormat.endTable();
  }

  private ServiceMethod processServiceMethod(Method SIGNATURE, Class<?> clazz, Method method, String comment,
      Module module) throws Exception {
    var serviceMethodName = method.getDeclaringClass().getSuperclass().getSimpleName().replace("ImplBase", "")
        + "." + method.getName();
    var collector = new AggregateCollector(classLoader, config.getBasePackage());
    var signature = (String) SIGNATURE.invoke(method);
    classScanner.walkCallTree(clazz.getName(), method.getName(), signature, collector, new ArrayDeque<>());
    var requiredPermission = getRequiredPermissions(method);
    var conditionalPermissions = collector.getPermissionCollector().getPermissions();
    var grpcServiceCalls = getGrpcServiceCalls(collector);
    var sourceLink = getSourceLink(method, module, collector);
    return ServiceMethod.builder()
        .name(serviceMethodName)
        .sourceLink(sourceLink)
        .requiredPermission(requiredPermission)
        .conditionalPermissions(conditionalPermissions)
        .grpcServiceCalls(grpcServiceCalls)
        .databaseTables(collector.getJooqTableCollector().getTables())
        .comment(comment)
        .build();
  }

  private String getRequiredPermissions(Method method) {
    var preAuthorize = method.getAnnotation(PreAuthorize.class);
    if (preAuthorize == null) {
      preAuthorize = method.getDeclaringClass().getAnnotation(PreAuthorize.class);
    }
    return preAuthorize != null
        ? preAuthorize.value().replace("hasAuthority('", "").replace("')", "")
        : "";
  }

  private List<String> getGrpcServiceCalls(AggregateCollector collector) {
    return collector.getMethodCollector().getReferencedMethods().stream().map(
        ref -> Arrays.stream(ref.getClassName().split("[\\.\\$]"))
            .filter(s -> s.contains("Grpc")).findFirst().orElse("").replace("Grpc", "." + ref.getMethodName()))
        .collect(Collectors.toUnmodifiableList());
  }

  private String getSourceLink(Method method, Module module, AggregateCollector collector) {
    return Optional.ofNullable(collector.getSourceFile())
        .filter(sourceFile -> sourceFile.equals(method.getDeclaringClass().getSimpleName() + ".java"))
        .filter(ignore -> collector.getLineNumber() > 0)
        .filter(ignore -> config.getSourceControlBaseUrl() != null && !config.getSourceControlBaseUrl().isBlank())
        .map(ignore -> config.getSourceControlBaseUrl() + "/"
            + module.getSourceCodeFolder() + "/"
            + method.getDeclaringClass().getName().replace(".", "/") + ".java#L"
            + collector.getLineNumber())
        .orElse("#");
  }
}
