package com.github.hervian.rip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hervian.rip.tasks.GenerateClientTask;
import com.github.hervian.rip.tasks.GenerateDocTask;
import com.github.hervian.rip.tasks.GenerateUiTask;
import com.github.hervian.rip.tasks.Task;
import com.github.hervian.rip.tasks.config.GenerateRestConfig;
import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.GenerateDocConfig;
import com.github.hervian.swagger.config.GenerateUiConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;

@Mojo(name = "rip",
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
  defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
  threadSafe = true)
public class RestInPeaceMojo  extends AbstractMojo {

  private PropertiesReader propertiesReader;

  public RestInPeaceMojo() throws MojoExecutionException {
    try {
      propertiesReader = new PropertiesReader();
    } catch (IOException e) {
      throw new MojoExecutionException("Error when constructing PropertiesReader object used to read properties from pom file.", e);
    }
  }

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Component
  private MavenSession mavenSession;

  @Component
  private BuildPluginManager pluginManager;

    /*@Parameter(required = true)
    private List<String> resourcePackages;

    public enum AdditionalDoc { //https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/java/org/openapitools/codegen/languages
        NONE,
        HTML,
        HTML2,
        CWIKI //Confluence wiki docs
    }
    @Parameter(defaultValue = "HTML2")
    private List<AdditionalDoc> additionalDocs;*/

  @Parameter
  private GenerateDocConfig generateDocConfig;

  @Parameter
  private GenerateUiConfig generateUiConfig;

  @Parameter
  private GenerateClientConfig generateClientConfig;

  /*@Parameter
    private GenerateRestConfig wrapClientsConfig;*/

  List<String> listOfGoals;

  public enum TaskType {
    genDoc(new GenerateDocTask()),
    /*genUi(new GenerateUiTask()),*/
    genClient(new GenerateClientTask()),
    ;//wrap(new WrapClientsTask());

    @Getter
    private Task task;

    private <T extends Task> TaskType(T task){
      this.task = task;
    }
  }

  @Parameter
  private List<TaskType> tasks = Lists.newArrayList(TaskType.genDoc, TaskType.genClient);//, TaskType.wrap);

  @SneakyThrows
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    long mojoStartTime = System.currentTimeMillis();
    String tasksString = System.getProperty("tasks");
    System.out.println("System.getProperty(\"tasks\")="+tasksString);
    String[] tasksArray = tasksString==null ? null : tasksString.split(",");
    List<TaskType> validatedTasks = validateTasks(tasksArray);
    if (validatedTasks!=null){
      tasks = validatedTasks;
    }

    MojoExecutionSummary.MojoExecutionSummaryBuilder mojoExecutionSummaryBuilder = MojoExecutionSummary.builder();

    Task.TaskInput taskInput = buildTaskInput();
    for (TaskType taskType: tasks){
      long startTime = System.currentTimeMillis();
      getLog().info("\n\t<<< rip-maven-plugin: " + taskType.name() + " task >>>");
      taskType.getTask().execute(taskInput);
      long endTime = System.currentTimeMillis();
      mojoExecutionSummaryBuilder.taskExecutionSummary(
        TaskExecutionSummary
          .builder()
          .taskDuration((endTime-startTime)/1000)
          .taskName(taskType.getTask().getClass().getSimpleName())
          .build()
      );
    }
    long mojoEndTime = System.currentTimeMillis();
    mojoExecutionSummaryBuilder.mojoDuration((mojoEndTime-mojoStartTime)/1000);
    System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(mojoExecutionSummaryBuilder.build()));
  }

  private Task.TaskInput buildTaskInput() {
    LifecyclePhase lifecyclePhase = getLifecyclePhase();
    return Task.TaskInput
      .builder()
      .propertiesReader(propertiesReader)
      .lifecyclePhase(lifecyclePhase)
      .project(project)
      .log(getLog())
      .mavenSession(mavenSession)
      .pluginManager(pluginManager)
      .generateDocConfig(generateDocConfig)
      .generateUiConfig(generateUiConfig)
      .generateClientConfig(generateClientConfig)
      //.wrapClientsConfig(wrapClientsConfig)
      .build();
  }

  private LifecyclePhase getLifecyclePhase() {
    for (String goal: mavenSession.getGoals()){
      try {
        return LifecyclePhase.valueOf(goal.toUpperCase());
      } catch (Exception e){
        //Do nothing. The command 'mvn clean compile' will return this list of goals: {"clean", "compile"} but only compile is a LifecyclePhase.
      }
    }
    getLog().warn("No lifecycle phase found. Were expecting stuff like 'verify' or 'deploy' etc");
    return null;
  }

  private List<TaskType> validateTasks(String[] tasks) throws MojoExecutionException {
    //TODO;
    return null;
  }

  @Builder
  @Data
  public static class MojoExecutionSummary {
    private long mojoDuration;
    @Singular
    private List<TaskExecutionSummary> taskExecutionSummaries;
  }

  @Builder
  @Data
  public static class TaskExecutionSummary {
    private String taskName;
    private long taskDuration;
  }

}
