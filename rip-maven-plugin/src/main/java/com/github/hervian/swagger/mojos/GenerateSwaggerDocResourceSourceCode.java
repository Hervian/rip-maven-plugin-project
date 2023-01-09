package com.github.hervian.swagger.mojos;
/*
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.InputStreamFacade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// maven properties: https://stackoverflow.com/a/13356378/6095334
@Mojo(name = "generateSwaggerDocResourceSourceCode", defaultPhase = LifecyclePhase.GENERATE_SOURCES, configurator = "include-project-dependencies",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true)*/
public class GenerateSwaggerDocResourceSourceCode {
   /* extends ApiDocumentMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  *//**
   * <p>
   * Specify where to place the generated source file, i.e. SwaggerDocResource.java .
   * </p>
   *//*
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/annotations")
  private File generatedSourcesDirectory;

  *//*protected void addGeneratedSources(MavenProject project) {
    List<String> roots = project.getCompileSourceRoots();
    String root = generatedSourcesDirectory.getAbsolutePath();
    if (!roots.contains(root)) {
      roots.add(root);
    }
  }*//*

  @SneakyThrows
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
  //  String pathToResource = GenerateSwaggerDocResourceSourceCode.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/GetSwaggerUiResource.java";

    getLog().info("generatedSourcesDirectory exists? " + generatedSourcesDirectory.exists() + ", file path:" + generatedSourcesDirectory.getAbsolutePath());

    *//*URL fileUrl = GenerateSwaggerDocResourceSourceCode.class.().getCodeSource().getLocation();
    File file = new File(fileUrl.toURI());
    getLog().info(file.getAbsolutePath());
    getLog().info(file.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath());*//*
    *//*InputStream streamToClass = classloader.getResourceAsStream("com/github/hervian/swagger/GetSwaggerUiResource.class");
    getLog().info("streamToClass!=null: " + Boolean.toString(streamToClass!=null));
*//*
    InputStream streamToJava =  getClass().getResourceAsStream("/SwaggerDocResource.java");
    getLog().info("streamToJava!=null: " + Boolean.toString(streamToJava!=null));

   *//* final Resource resource = new Resource();
    resource.setDirectory("target/generated-resources/some");
    project.getBuild().getSourceDirectory()..getResources().add(resource);*//*

    *//*InputStream inputUrl = classloader.getResourceAsStream("./SwaggerDocResource.java");
    getLog().info("step 1");
    getLog().info("inputUrl!=null: " + Boolean.toString(inputUrl!=null));
    getLog().info("step 2");
    InputStream inputUrl2 = getClass().getResourceAsStream("SwaggerDocResource.java");
    getLog().info("step 3");
    getLog().info("inputUrl2!=null: " + Boolean.toString(inputUrl2!=null));
    getLog().info("step 4");*//*
    //File dest = new File(project.getBuild().getOutputDirectory() + "/../generated-sources/annotations/"+ "/com/github/hervian/swagger/copy/SwaggerDocResource.java");// + "/generated-sources");
    File dest2 = new File(generatedSourcesDirectory.getPath()+ "/dk/tdc/chub/SwaggerDocResource.java");// + "/generated-sources"); //TODO: create configurable package and remember to update package in copy-pasted file
    getLog().info("Copying SwaggerDocResource.java from the plugin's resource folder to your project at configured location.");

    FileUtils.copyStreamToFile(new InputStreamFacade() {

      @Override
      public InputStream getInputStream() throws IOException
      {
        return streamToJava;
        //return new FileInputStream(generatedSourcesDirectory);
      }
    }, dest2);
    addAnnotationsIfFoundOnClassPath(dest2); //TODO: use some template language/framework

    getLog().info(String.format("Copied SwaggerDocResource.java to %s", dest2.getAbsolutePath()));
    //project.addCompileSourceRoot(project.getBuild().getOutputDirectory() + "/../generated-sources/annotations/"); //This line results in a maven build returning lots of compile errors. SPecifically: Duplicate class error for SwaggerDocResource
    project.addCompileSourceRoot(generatedSourcesDirectory.getPath());
    *//*addGeneratedSources(project);*//*
  }

  private void addAnnotationsIfFoundOnClassPath(File dest) {
    boolean addApiOperationAnnotation = isOnClassPath("io.swagger.annotations.ApiOperation");
    boolean addConditionalOnPropertyAnnotation = isOnClassPath("org.springframework.boot.autoconfigure.condition.ConditionalOnProperty");
    boolean addGeneratedAnnotation = isOnClassPath("javax.annotation.Generated");

    try {
      String content = org.apache.commons.io.FileUtils.readFileToString(dest, "UTF-8");

      content = addOrRemoveAnnotation(addApiOperationAnnotation, content, "ApiOperation");
      content = addOrRemoveAnnotation(addConditionalOnPropertyAnnotation, content, "ConditionalOnProperty");
      content = addOrRemoveAnnotation(addGeneratedAnnotation, content, "Generated");

      org.apache.commons.io.FileUtils.writeStringToFile(dest, content, "UTF-8");
    } catch (IOException ex) {
      getLog().error(ex);
      throw new RuntimeException("Generating file failed", ex);
    }

    getLog().info("addApiOperationToHideEndpointIfApiOperationTypeIsOnClasspath");
  }

  private boolean isOnClassPath(String fqcn) {
    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      Class.forName(fqcn, false, classloader);
      return true;
    } catch(ClassNotFoundException e) {
      return false;
    }
  }

  private String addOrRemoveAnnotation(boolean addAnnotation, String content, String simpelName) {
    if (addAnnotation) {
      content = content.replaceAll(String.format("//%s:", simpelName), "");
    } else {
      Matcher matcher = Pattern.compile(String.format(".*%s.*", simpelName), Pattern.MULTILINE).matcher(content);
      content = matcher.replaceAll("");
    }
    return content;
  }

  public static void main(String[] args){
    String content = "import ConditionalOnProperty\n" +
        "ConditionalOnProperty asdfdsa \n" +
        "asdf asdf asdf \n" +
        "Condition ConditionalOnProperty sfdf\n" +
        "CondiConditionalOnPropert ConditionalOnProper";
    Matcher matcher = Pattern.compile(".*ConditionalOnProperty.*",Pattern.MULTILINE).matcher(content);
    content = matcher.replaceAll("");
    System.out.println(content);
  }
*/
}
