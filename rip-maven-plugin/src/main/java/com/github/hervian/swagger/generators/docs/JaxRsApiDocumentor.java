package com.github.hervian.swagger.generators.docs;

import com.github.hervian.swagger.util.MojoExecutorWrapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.ArrayList;
import java.util.List;

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

public class JaxRsApiDocumentor implements OpenApiDocumentGenerator {

  @Override
  public void generate(DocumentGeneratorInput documentGeneratorInput) throws MojoExecutionException {
    MavenProject project = documentGeneratorInput.getProject();
    List<String> resourcePackages = documentGeneratorInput.getGenerateDocConfig().getResourcePackagesWithFallback(project);
    documentGeneratorInput.getLog().info("Scanning for rest resources in packages: " + String.join(",", resourcePackages));
    MojoExecutor.Element[] resourcePackagesAsElements = new MojoExecutor.Element[resourcePackages.size()];
    for (int i=0; i<resourcePackages.size(); i++){
      resourcePackagesAsElements[i]=(element(name("resourcePackage"), resourcePackages.get(i)));
    }
    List<Dependency> dependencies = new ArrayList<>();
    Dependency reflectionsDependency = new Dependency();
    reflectionsDependency.setGroupId("org.reflections");
    reflectionsDependency.setArtifactId("reflections");
    reflectionsDependency.setVersion("0.10.2");
    dependencies.add(reflectionsDependency);
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("io.openapitools.swagger"),
        artifactId("swagger-maven-plugin"),//https://github.com/openapi-tools/swagger-maven-plugin
        version("2.1.6"),//TODO: get values from PropertiesReader
        dependencies
      ),
      goal("generate"),
      configuration(
        element(name("outputDirectory"), documentGeneratorInput.getOutputDir()),
        element(name("outputFilename"), "swagger"),
        element(name("outputFormats"), "JSON"),
        element(name("prettyPrint"), "true"),
        element(name("resourcePackages"), resourcePackagesAsElements)
        ,element(name("swaggerConfig"), //See https://github.com/openapi-tools/swagger-maven-plugin. Note that the invoked mojo also picks up any OpenApiDefinition annotation which is another way to configure the open api json meta data
          element(name("info"),
            element(name("title"), project.getName() + " REST API"),
            element(name("version"), project.getVersion()),
            element(name("description"), project.getDescription()))
        )
      ),
      executionEnvironment(
        project,
        documentGeneratorInput.getMavenSession(),
        documentGeneratorInput.getPluginManager()
      ));
  }

}
