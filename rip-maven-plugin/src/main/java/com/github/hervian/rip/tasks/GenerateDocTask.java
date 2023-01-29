package com.github.hervian.rip.tasks;

import com.github.hervian.rip.GenerateDocMojo;
import com.google.common.collect.Lists;
import org.apache.maven.plugin.MojoExecutionException;

public class GenerateDocTask implements Task {

  @Override
  public void execute(TaskInput taskInput) throws MojoExecutionException {
    GenerateDocMojo mojo = new GenerateDocMojo();
    mojo.setProject(taskInput.getProject());
    mojo.setMavenSession(taskInput.getMavenSession());
    mojo.setPluginManager(taskInput.getPluginManager());
    mojo.setPropertiesReader(taskInput.getPropertiesReader());
    if (taskInput.getLifecyclePhase()!=null){
      mojo.setListOfGoals(Lists.newArrayList(taskInput.getLifecyclePhase().name()));
    }
    mojo.setLog(taskInput.getLog());
    mojo.setGenerateDocConfig(taskInput.getGenerateDocConfig());
    mojo.setGenerateClientConfig(taskInput.getGenerateClientConfig());

    mojo.execute();
  }

}
