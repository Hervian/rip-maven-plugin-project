package com.github.hervian.swagger.publishers;

import org.apache.maven.plugin.MojoExecutionException;

public class JavaMavenClientPublisher implements ClientPublisher {

  /**
   * http://techtraits.com/build%20management/maven/2012/06/24/Maven-Deploying-multiple-artifacts-from-one-build.html
   *
   * Artifactory
   * Nexus
   * Github Packages: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry
   */
  @Override
  public ClientPublisherOutput publish(ClientPublisherInput clientPublisherInput) throws MojoExecutionException {
    clientPublisherInput.getLog().info("Deploying client to binary repo.");
    /*executeMojo(
      plugin(
        groupId("org.apache.maven.plugins"),
        artifactId("maven-deploy-plugin"),
        version("3.0.0-M1")
      ),
      goal("deploy-file"),
      configuration(
        element(name("artifactId"), TODO),
        element(name("packaging"), "jar"),
        element(name("version"), project.getVersion()),
        element(name("groupId"), project.getGroupId()),
        element(name("classifier"), TODO),
        element(name("file"), target/path/to/client.jar),
        element(name("pomFile"), "pom.xml"),
        element(name("url"), project.getDistributionManagementArtifactRepository().getUrl()) //TODO: if snapshot use project.getDistributionManagementArtifactRepository().getSnapshot ...  and similarly for release
        //element(name("configOptions"), element(name(CodegenConstants.SOURCE_FOLDER), ""))
      ),
      executionEnvironment(
        project,
        mavenSession,
        pluginManager
      )
    );*/
    return null;
  }

}
