package com.github.hervian.rip.util.compilation;

import com.github.hervian.rip.doc.src.SwaggerDocJaxRsResource;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Builder
@Data
public class ClassFileCopier {
  private Log log;
  private List<String> resourcePackages;
  private MavenProject project;

  /*public void copyResourceToBuildOutputDir(Class<?> restResource, List<Class<?>> restAnnotationTypes) throws MojoExecutionException {
    //TODO: https://stackoverflow.com/a/2946402/6095334, https://stackoverflow.com/q/9665768/6095334, https://github.com/trung/InMemoryJavaCompiler
    // 1: load copy of source file from top level of jar and edit package name
    // 2: Compile edited code to a .class file, see https://stackoverflow.com/questions/4463440/compile-java-source-code-from-a-string

    // 4: Save to build output dir
    String resource = String.format("/%s.java", restResource.getSimpleName());//SwaggerDocJaxRsResource.class.getSimpleName()
    log.info("copying file: " + resource);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    //URL inputUrl = classloader.getResource("com/github/hervian/swagger/GetSwaggerUiResource.class");
    //InputStream sourceCodeStream = classloader.getResourceAsStream("/SwaggerDocJaxRsResource.java");
    InputStream sourceCodeStream = getClass().getResourceAsStream(resource);
    try {
      String sourceCode = IOUtils.toString(sourceCodeStream, StandardCharsets.UTF_8.name());

      *//*sourceCode = addAnnotations(sourceCode, restAnnotationType);*//*
      String newPackage = resourcePackages.get(0);
      getLog().info(String.format("Editing class %s by setting package to: %s", restResource.getName(), newPackage));
      sourceCode = editPackage(sourceCode, newPackage);
      String outputDirOfSwaggerDocJaxRsResource = project.getBuild().getOutputDirectory();// + "/"+ newPackage.replace(".", "/");

      StringToFileCompiler.compile(restResource.getSimpleName(), sourceCode, outputDirOfSwaggerDocJaxRsResource, restAnnotationTypes);
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException("Unable to copy generated resources to build output folder", e);
    }
  }*/

  public void copyResourceToBuildOutputDir(String className, String sourceCode, List<Class<?>> restAnnotationTypes) throws MojoExecutionException {
    try {
      String outputDirOfSwaggerDocJaxRsResource = project.getBuild().getOutputDirectory();// + "/"+ newPackage.replace(".", "/");

      StringToFileCompiler.compile(className, sourceCode, outputDirOfSwaggerDocJaxRsResource, restAnnotationTypes);
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException("Unable to copy generated resources to build output folder", e);
    }
  }

  /**
   * NB: This method only works due to the following plugin configured in the pom: maven-resources-plugin
   * Above plugin copy pastes select .java classes to the resource directory upon compilation. Thereby making them
   * accessible within the jar as resources on the top level. Excpect this logic to break if you move the classes to some other folder...
   *     //TODO: https://stackoverflow.com/a/2946402/6095334, https://stackoverflow.com/q/9665768/6095334, https://github.com/trung/InMemoryJavaCompiler
   *     // 1: load copy of source file from top level of jar and edit package name
   *     // 2: Compile edited code to a .class file, see https://stackoverflow.com/questions/4463440/compile-java-source-code-from-a-string
   * @param restResource
   * @return
   * @throws MojoExecutionException
   */
  public String getSourceCode(Class<?> restResource) throws MojoExecutionException {
    String resource = String.format("/%s.java", restResource.getSimpleName());//SwaggerDocJaxRsResource.class.getSimpleName()
    log.info("copying file: " + resource);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    //URL inputUrl = classloader.getResource("com/github/hervian/swagger/GetSwaggerUiResource.class");
    //InputStream sourceCodeStream = classloader.getResourceAsStream("/SwaggerDocJaxRsResource.java");
    InputStream sourceCodeStream = getClass().getResourceAsStream(resource);

    String sourceCode = null;
    try {
      sourceCode = IOUtils.toString(sourceCodeStream, StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new MojoExecutionException(String.format("Exception thrown when getting source code for class %s", restResource.getName()), e);
    }

    /*sourceCode = addAnnotations(sourceCode, restAnnotationType);*/
    String newPackage = resourcePackages.get(0);
    getLog().info(String.format("Editing class %s by setting package to: %s", restResource.getName(), newPackage));
    return sourceCode = editPackage(sourceCode, newPackage);
  }

  private String editPackage(String sourceCode, String newPackage) {
    String oldPackage = SwaggerDocJaxRsResource.class.getPackage().getName();
    return sourceCode.replace(oldPackage, newPackage);
  }

 /* //TODO: I am not sure this method works  - is it the classpath of the plugin we are using or that of the project using the plugin? (We want the latter). In other words: Likely this plugin pt only works for Spring projects
  private String addAnnotations(String content, GenerateDocConfig.RestAnnotationType restAnnotationType) {
    boolean addApiOperationAnnotation = isOnClassPath("io.swagger.annotations.ApiOperation");
    boolean addConditionalOnPropertyAnnotation = isOnClassPath("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty");
    boolean addGeneratedAnnotation = isOnClassPath("javax.annotation.Generated");

    content = addOrRemoveAnnotation(addApiOperationAnnotation, content, "ApiOperation");
    content = addOrRemoveAnnotation(addConditionalOnPropertyAnnotation, content, "ConditionalOnProperty");
    content = addOrRemoveAnnotation(addGeneratedAnnotation, content, "Generated");

    return content;
  }

  private boolean isOnClassPath(String fqcn) {
    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      Class.forName(fqcn, false, classloader);
      getLog().info(fqcn + " was found on the classpath");
      return true;
    } catch(ClassNotFoundException e) {
      return false;
    }
  }*/

 /* private String addSpringRestAnnotations(String content

  private String addOrRemoveAnnotation(boolean addAnnotation, String content, String simpelName) {
    if (addAnnotation) {
      getLog().info("adding annotation: " + simpelName);
      content = content.replaceAll(String.format("//%s:", simpelName), "");
    } else {
      getLog().info("removing annotation: " + simpelName);
      Matcher matcher = Pattern.compile(String.format(".*%s.*", simpelName), Pattern.MULTILINE).matcher(content);
      content = matcher.replaceAll("");
    }
    return content;
  }*/

}
