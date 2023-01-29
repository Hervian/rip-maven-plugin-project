package com.github.hervian.rip.tasks;

import com.github.hervian.rip.GenerateClientMojo;
import org.apache.maven.plugin.MojoExecutionException;

public class GenerateClientTask implements Task {

  @Override
  public void execute(TaskInput taskInput) throws MojoExecutionException {
    GenerateClientMojo mojo = new GenerateClientMojo();
    mojo.setProject(taskInput.getProject());
    mojo.setMavenSession(taskInput.getMavenSession());
    mojo.setPluginManager(taskInput.getPluginManager());
    mojo.setPropertiesReader(taskInput.getPropertiesReader());
    mojo.setLog(taskInput.getLog());
    mojo.setGenerateClientConfig(taskInput.getGenerateClientConfig());

    mojo.execute();
  }

}
