package ch.retomock.docgen.scanner;

import ch.retomock.docgen.config.DocGenConfigException;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class JavaDocSourceScanner {

  public static final JavaParser JAVA_PARSER = new JavaParser();

  public static Map<String, String> collectJavaDoc(String projectRootDirectory, String[] javaDocSourceDirectories)
      throws FileNotFoundException {
    Map<String, String> javaDoc = new HashMap<>();
    for (var javaDocSourceDirectory : javaDocSourceDirectories) {
      var directory = new File(projectRootDirectory, javaDocSourceDirectory);
      System.out.println("Collecting JavaDoc from " + directory.getAbsolutePath());
      scan(directory, javaDoc);
    }
    if (javaDoc.isEmpty()) {
      throw new DocGenConfigException("No Javadoc for gRPC services found. Please check your config.");
    }
    return javaDoc;
  }

  private static void scan(File file, Map<String, String> javaDoc) throws FileNotFoundException {
    if (file.isDirectory()) {
      for (var child : file.listFiles()) {
        scan(child, javaDoc);
      }
    } else {
      if (isGrpcServiceClass(file)) {
        var visitor = new VoidVisitorAdapter<>() {
          @Override
          public void visit(JavadocComment comment, Object arg) {
            super.visit(comment, arg);
            if (comment.getCommentedNode().isPresent()) {
              collectJavadoc(comment.getCommentedNode().get(), comment, javaDoc);
            }
          }
        };
        visitor.visit(JAVA_PARSER.parse(file).getResult().get(), null);
      }
    }
  }

  private static void collectJavadoc(Node node, JavadocComment comment, Map<String, String> javaDoc) {
    if (node instanceof MethodDeclaration) {
      var methodDeclaration = (MethodDeclaration) node;
      if (isGrpcMethod(methodDeclaration)) {
        var serviceName = ((ClassOrInterfaceDeclaration) methodDeclaration.getParentNode().get()).getNameAsString();
        var methodName = methodDeclaration.getNameAsString();
        var serviceMethod = serviceName + "." + methodName;
        var javaDocComment = comment.asJavadocComment().parse().getDescription().toText();
        javaDoc.put(serviceMethod, javaDocComment);
      }
    }
  }

  private static boolean isGrpcServiceClass(File file) {
    return file.getName().endsWith("Grpc.java");
  }

  private static boolean isGrpcMethod(MethodDeclaration methodDeclaration) {
    return methodDeclaration.getParameters().getLast()
        .filter(p -> p.getNameAsString().equals("responseObserver"))
        .isPresent();
  }
}
