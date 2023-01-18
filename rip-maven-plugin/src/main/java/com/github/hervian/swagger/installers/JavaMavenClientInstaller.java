package com.github.hervian.swagger.installers;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.generators.ClientGenerator;
import com.github.hervian.swagger.generators.ClientGeneratorOutput;
import com.github.hervian.swagger.mojos.GenerateClientMojo;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

public class JavaMavenClientInstaller implements ClientInstaller {

  //https://maven.apache.org/shared/maven-invoker/usage.html
  @Override
  public ClientInstallerOutput install(ClientInstallerInput clientInstallerInput) throws MojoExecutionException {
    clientInstallerInput.getLog().info("Installing client in local repository. TODO...");

    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile( new File( getClientPomFile(clientInstallerInput) ) );
    request.setBaseDirectory( new File(ClientGenerator.getOutputPath( clientInstallerInput.getProject())+"/java")); //TODO: make more generic. What about fx dart?

    //request.setGoals( Arrays.asList( "clean", "install" ) );
    String pathToRipMavenPluginJar = "";
    try {
      pathToRipMavenPluginJar = new File(GenerateClientMojo.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
      pathToRipMavenPluginJar.replace("\\", "/");
      clientInstallerInput.getLog().info("rip-maven-plugin jar path: " + pathToRipMavenPluginJar);
    } catch (URISyntaxException e) {
      throw new MojoExecutionException(e);
    }
    //TODO: work in progress. Trying to compile WITH abstractprocessor
    request.setGoals( Arrays.asList( "clean", "install", "-DMAVEN_OPTS=\"-processorpath "+pathToRipMavenPluginJar+"\"\" ) );//path/to/mapstruct-processor-1.3.0.Beta1.jar\"" ) );
    MavenExecutionRequest mavenExecutionRequest = clientInstallerInput.getMavenSession().getRequest();
    /*File globalSettingsFile = mavenExecutionRequest.getGlobalSettingsFile();
    File userSettingsFile = mavenExecutionRequest.getUserSettingsFile();
    request.setGlobalSettingsFile(globalSettingsFile);*/
    Invoker invoker = new DefaultInvoker();
    try {
      invoker.execute( request );
    } catch (MavenInvocationException e) {
      e.printStackTrace();
    }

    /*MavenProject clientProject = new MavenProject();
    project.setFile( new File(project.getBuild().getDirectory()+"/temp") );
    executeMojo(
      plugin(
        groupId("org.apache.maven.plugins"),
        artifactId("maven-install-plugin"),
        version("3.0.0-M1") //TODO: get as property
      ),
      goal("install"),
      configuration(
        //element(name("artifactId"), TODO),
        //element(name("packaging"), "jar"),
        //element(name("version"), project.getVersion()),
        //element(name("groupId"), project.getGroupId()),
       // element(name("classifier"), TODO),
        //element(name("pomFile"), getClientPomFile())
        //element(name("pomFile"), "pom.xml"),
        //element(name("url"), project.getDistributionManagementArtifactRepository().getUrl()) //TODO: if snapshot use project.getDistributionManagementArtifactRepository().getSnapshot ...  and similarly for release
        //element(name("configOptions"), element(name(CodegenConstants.SOURCE_FOLDER), ""))
      ),
      executionEnvironment(
        clientProject,
        mavenSession,
        pluginManager
      )
    );*/
    return null;
  }

  private String getClientPomFile(ClientInstallerInput clientInstallerInput){
    String pathToClientPom = ClientGenerator.getOutputPath( clientInstallerInput.getProject()) +"/pom.xml";
    clientInstallerInput.getLog().info("Path to pom.xml of generated client code: " + pathToClientPom);
    return pathToClientPom;
  }

}
