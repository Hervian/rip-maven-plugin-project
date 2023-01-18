package com.github.hervian.swagger.generators;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.util.MojoExecutorWrapper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openapitools.codegen.CodegenConstants;

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

public class DartClientGenerator implements ClientGenerator {

  public ClientGeneratorOutput generateClient(ClientGeneratorInput clientGeneratorInput) throws MojoExecutionException {
    clientGeneratorInput.getLog().info("Genering Dart client code from " + clientGeneratorInput.getPathToSwaggerDoc());
    MavenProject project = clientGeneratorInput.getProject();
    String outputPath = ClientGenerator.getOutputPath( project) + "/dart";
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.openapitools"),
        artifactId("openapi-generator-maven-plugin"),
        version(clientGeneratorInput.getPropertiesReader().getOrgOpenapitoolsVersion())
      ),
      goal("generate"),
      configuration( //see https://openapi-generator.tech/docs/generators/java/
        element(name("inputSpec"), clientGeneratorInput.getPathToSwaggerDoc()),
        element(name("generatorName"), "dart"),
        element(name("output"), outputPath),
        element(name("configOptions"),
          //element(name(CodegenConstants.ALLOW_UNICODE_IDENTIFIERS), "true"),
          element(name("pubVersion"), clientGeneratorInput.getProject().getVersion()),
          element(name("pubName"),  (clientGeneratorInput.getProject().getArtifactId() + "-client").replaceAll("-", "_")),
          //element(name("pubLibrary"), TODO),
          element(name("pubDescription"), "Generated dart client for the server " + clientGeneratorInput.getProject().getArtifactId())
          //element(name("pubAuthor"), TODO)
          //,element(name("useBeanValidation"), "true")
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

    return ClientGeneratorOutput.builder().language(GenerateClientConfig.Language.DART).path(outputPath).build();
  }

}
