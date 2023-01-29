package com.github.hervian.rip.tasks;

import com.github.hervian.rip.GenerateUiMojo;
import org.apache.maven.plugin.MojoExecutionException;

public class GenerateUiTask implements Task {

  @Override
  public void execute(TaskInput taskInput) throws MojoExecutionException {
    GenerateUiMojo mojo = new GenerateUiMojo();
    mojo.setProject(taskInput.getProject());
    mojo.setPropertiesReader(taskInput.getPropertiesReader());
    mojo.setLog(taskInput.getLog());
    mojo.setGenerateDocConfig(taskInput.getGenerateDocConfig());
    mojo.setGenerateUiConfig(taskInput.getGenerateUiConfig());

    mojo.execute();
  }

}
