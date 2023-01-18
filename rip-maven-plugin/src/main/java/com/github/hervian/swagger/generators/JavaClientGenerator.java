package com.github.hervian.swagger.generators;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import com.github.hervian.swagger.util.MojoExecutorWrapper;
import lombok.Builder;
import lombok.Data;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.openapitools.codegen.CodegenConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

public class JavaClientGenerator implements ClientGenerator {

  public ClientGeneratorOutput generateClient(ClientGeneratorInput clientGeneratorInput) throws MojoExecutionException {
    String path = generateClientSourceCode(clientGeneratorInput);
    /*try {
      postProcessGeneratedSourceCode(clientGeneratorInput, path);
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }*/
    //make sure the generated client is not considered part of the IDEs sources:
    System.out.println("printing project.getCompileSourceRoots():");
    for (Object object: clientGeneratorInput.getProject().getCompileSourceRoots()) {
      System.out.println(object);
    }
    return ClientGeneratorOutput.builder().language(GenerateClientConfig.Language.JAVA).path(path).build();
  }


  private void postProcessGeneratedSourceCode(ClientGeneratorInput clientGeneratorInput, String outputFolder) throws IOException {
    //Search and replace Generated annotation with custom anno + Generated
    Path path = Paths.get(outputFolder);
    Charset charset = StandardCharsets.UTF_8;
Path apiClient = null;
    try (Stream<Path> walk = Files.walk(path)) {
      apiClient = walk.filter(e-> Files.isRegularFile(e) && e.getFileName().toString().equals("ApiClient")).findFirst().get();
    }

    String content = new String(Files.readAllBytes(apiClient), charset);
    content = content.replaceAll("@javax.annotation.Generated", "@com.github.hervian.rip.tasks.ap.OpenApiGeneratedClient @javax.annotation.Generated");
    Files.write(path, content.getBytes(charset));
    /*executeMojo(
      plugin(
        groupId("org.bsc.maven"),
        artifactId("maven-processor-plugin"),
        version(clientGeneratorInput.getPropertiesReader().getOrgOpenapitoolsVersion())
      ),
      goal("process"),
      configuration(
        element(name("processors"),
          element(name("processor"), Ope)
        )
      ),
      executionEnvironment(
        clientGeneratorInput.getProject(),
        clientGeneratorInput.getMavenSession(),
        clientGeneratorInput.getPluginManager()
      )
    );*/
  }

  private String generateClientSourceCode(ClientGeneratorInput clientGeneratorInput) throws MojoExecutionException {
    clientGeneratorInput.getLog().info("Genering Java client code from " + clientGeneratorInput.getPathToSwaggerDoc());
    MavenProject project = clientGeneratorInput.getProject();
    String outputPath = ClientGenerator.getOutputPath(project) + "/java";
//Can this resource be used for better client generation=´? https://arnoldgalovics.com/swagger-codegen-feign-gradle/
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.openapitools"),
        artifactId("openapi-generator-maven-plugin"),
        version(clientGeneratorInput.getPropertiesReader().getOrgOpenapitoolsVersion())
      ),
      goal("generate"),
      configuration( //see https://openapi-generator.tech/docs/generators/java/
        element(name("inputSpec"), clientGeneratorInput.getPathToSwaggerDoc()),
        element(name("generatorName"), "java"),
        element(name("output"), outputPath),
        element(name("groupId"), project.getGroupId()),
        element(name("artifactId"), project.getArtifactId() + "-client"),
        element(name("artifactVersion"), project.getVersion()),
        //element(name(CodegenConstants.ALLOW_UNICODE_IDENTIFIERS), "true"),
        element(name("configOptions"),
          element(name(CodegenConstants.ALLOW_UNICODE_IDENTIFIERS), "true"),
          element(name("useBeanValidation"), "true"),
          element(name("openApiNullable"), "false"), //WebClient library produces: java: package org.openapitools.jackson.nullable does not exist
          //element(name("performBeanValidation"), "true"), will probably only work if there is a bean validation impl on classpath?
          element(name("library"), clientGeneratorInput.getGenerateClientConfig().getJavaConfig().getLibrary()), //TODO: Use 'webclient'? And add circuit breaking somehow?
          //element(name("useSwaggerUI"), "true"), what does this do? Default is true
          element(name("useTags"), "true"),
          element(name("invokerPackage"), project.getGroupId() + "." + project.getArtifactId()),
          element(name("apiPackage"), project.getGroupId() + "." + project.getArtifactId())
        )
      ),
      executionEnvironment(
        project,
        clientGeneratorInput.getMavenSession(),
        clientGeneratorInput.getPluginManager()
      )
    );
    //GenerateClientMojo generateClientMojo = new GenerateClientMojo();
    //generation pt happens in a separate Mojo which the user of this library must enable by adding <goal>generateAndDeployClient</goal>

    return outputPath;
  }


}
