package com.github.hervian.rip;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Fails the build if the pom file's SemVer version number's major has NOT increased BUT a breaking change has been introduced in the REST API.
 * That is, this mojo compares the current code's swagger.json with that of the inferred previous version.
 */
@Mojo(name = "diff",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    threadSafe = true)
public class DiffMojo extends AbstractMojo {
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

  }
}
