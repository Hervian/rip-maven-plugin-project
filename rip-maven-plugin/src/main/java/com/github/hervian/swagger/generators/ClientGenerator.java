package com.github.hervian.swagger.generators;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public interface ClientGenerator {

  ClientGeneratorOutput generateClient(ClientGeneratorInput clientGeneratorInput) throws MojoExecutionException;

  static String getOutputPath(MavenProject project) {
    return project.getBuild().getDirectory()+"/temp";
  }

}
