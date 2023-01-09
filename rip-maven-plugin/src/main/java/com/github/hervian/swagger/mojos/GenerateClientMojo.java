package com.github.hervian.swagger.mojos;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import com.github.hervian.swagger.generators.ClientGenerator;
import com.github.hervian.swagger.generators.ClientGeneratorInput;
import com.github.hervian.swagger.generators.JavaClientGenerator;
import com.github.hervian.swagger.generators.DartClientGenerator;
import io.openapitools.swagger.OutputFormat;
import io.redskap.swagger.brake.core.CoreConfiguration;
import io.redskap.swagger.brake.maven.MavenConfiguration;
import io.redskap.swagger.brake.report.ReporterConfiguration;
import io.redskap.swagger.brake.runner.ArtifactPackaging;
import io.redskap.swagger.brake.runner.Options;
import io.redskap.swagger.brake.runner.RunnerConfiguration;
import io.redskap.swagger.brake.runner.download.ArtifactDownloaderHandler;
import io.redskap.swagger.brake.runner.exception.LatestArtifactDownloadException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.openapitools.codegen.CodegenConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.crypto.spec.PSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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

@Data
@Mojo(name = "generateClient", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class GenerateClientMojo extends AbstractMojo {
  private final Logger LOGGER = LoggerFactory.getLogger(GenerateClientMojo.class);

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  /*@Parameter
  private List<String> resourcePackages;*/

  @Component
  private MavenSession mavenSession;

  @Component
  private BuildPluginManager pluginManager;

  @Parameter
  GenerateClientConfig generateClientConfig = new GenerateClientConfig();

  @Component
  private MojoExecution execution;

  private PropertiesReader propertiesReader;

  public GenerateClientMojo() throws MojoExecutionException {
    try {
      propertiesReader = new PropertiesReader();
    } catch (IOException e) {
      throw new MojoExecutionException("Error when constructing PropertiesReader object used to read properties from pom file.", e);
    }
  }

  @Override
  public void execute() throws MojoExecutionException {
    validatePhaseAndGoals();

    List<String> cmdLineGoals = getMavenPhase();
    getLog().info("command line goals: " + cmdLineGoals);
    boolean apiIsDifferentThanLastVersion;
    if (cmdLineGoals.contains("swagger:generateClient") || (apiIsDifferentThanLastVersion=apiIsDifferentThanLastVersion() && phaseIsLaterThanCompile(cmdLineGoals))){ //TODO: what about package goal?
      getLog().info("generating a client code for your server since new api contains changes compared to last version.");
//TODO: If java genJavaClient, if Dart genDartClient
      String outputPath = generateClient();
      //postProcessClient(outputPath);
      //if (generateClientConfig.getLanguages().contains(GenerateClientConfig.Language.JAVA)){//install/deploy java
      if (cmdLineGoals==null || cmdLineGoals.contains("install")){
        installClient();
      } else if (cmdLineGoals.contains("deploy")){
        deployClient();
      }
    } else {
      String msg;
      if (!apiIsDifferentThanLastVersion){
        msg = "Skipping client code generation since no changes were found between new swagger doc and last swagger doc as downloaded from binary repo.";
      } else {
        msg = "Skipping client code generation since goal is neither 'install', nor 'deploy'";
      }
      getLog().info(msg);
    }
  }

  private void postProcessClient(String outputPath) {
    //TODO: for Java clients: Make the generated client injectable by
    //  A) Making the project a "spring boot starter": https://www.baeldung.com/spring-boot-custom-starter
    //  B) Creating a bean of the client api. Feign: https://resilience4j.readme.io/docs/feign#fallback
          // make the generated builders fields public and map the ApiClient builder to Resilience4jFeign.builder
  }

  private boolean phaseIsLaterThanCompile(List<String> cmdLineGoals) {
    for (String goal: cmdLineGoals){
      try {
        if (LifecyclePhase.valueOf(goal.toUpperCase()).ordinal()>LifecyclePhase.COMPILE.ordinal()){
          return true;
        }
      } catch (Exception e){
        //Do nothing, fx calling LifecyclePhase.valueOf("clean") will throw exception  No enum constant org.apache.maven.plugins.annotations.LifecyclePhase.clean
      }
    }
    return false;
  }

  private List<String> getMavenPhase() {
    return mavenSession.getGoals();
  }

  /**
   * The creation of the annotation context is copy-pasted from the swagger-brake-maven-plugin, seeio.redskap.swagger.brake.runner.{@link io.redskap.swagger.brake.runner.Starter}
   * @return
   * @throws MojoExecutionException
   */
  private boolean apiIsDifferentThanLastVersion() throws MojoExecutionException {
    //if breaking changes check was disabled, run now.
    //otherwise, check if the generated diff report (/target/swagger-brake/swagger-brake.html) contains the string "API is backward compatible"
    //See https://redskap.github.io/swagger-brake/maven/#customizing-reporting
    /*try {
      String breakingChangesReport = new String(Files.readAllBytes(Paths.get(project.getBuild().getDirectory()+"/swagger-brake/swagger-brake.html")), StandardCharsets.UTF_8);
      return !breakingChangesReport.contains("API is backward compatible");
    } catch (IOException e) {
      throw new MojoExecutionException("Exception when trying to parse the swagger-brake.html file to inspect if there is any API changes (with the aim of determining whether or not to generate a new client)", e);
    }*/


    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RunnerConfiguration.class, ReporterConfiguration.class, MavenConfiguration.class, CoreConfiguration.class);
    //Alternative: TemporaryJarFileDownloader from swagger-brake-root project or Maven2LatestArtifactDownloader
    ArtifactDownloaderHandler artifactDownloaderHandler = applicationContext.getBean(ArtifactDownloaderHandler.class);
    Options options = new Options();
    options.setMavenRepoUrl(GenerateDocMojo.getReleasesRepo(project));
    options.setMavenSnapshotRepoUrl(GenerateDocMojo.getSnapshotRepo(project));
    options.setGroupId(project.getGroupId());
    options.setArtifactId(project.getArtifactId());
    options.setCurrentArtifactVersion(project.getVersion());
    options.setApiFilename("swagger.json");
    options.setArtifactPackaging(ArtifactPackaging.forPackaging(project.getPackaging()));

    try {
      artifactDownloaderHandler.handle(options);
    } catch (LatestArtifactDownloadException e){
      if (e.getCause() instanceof RuntimeException && e.getCause().getMessage().contains("Cannot get metadata")){
        getLog().info("A 'Cannot get metadata' error thrown by the swagger-brake.jar that we use to download the last version of your code. That error typically means that you have not yet pushed any binary to the remote repo, for which reason we simply ignore the error and interprets your current API to have diffs as compared to the previous (non-existing) version such as to trigger generation of a new client.");
        return true; //TODO: This is really a bug in the swagger-brake library and should be reported on github - after a fix we can upgrade the dependency and delete this 'hack'
      } else {
        throw e;
      }
    }

    System.out.println("Path to old api file: " + options.getOldApiPath());

    String oldApi = options.getOldApiPath(), newApi = getPathToSwaggerDoc();
    getLog().info("oldApi path = " + oldApi);
    getLog().info("newApi path = " + newApi);
    try {

      OpenAPIV3Parser parser = new OpenAPIV3Parser();
      OpenAPI oldApiAsOpenApi = parser.read(oldApi), newApiAsOpenApi = parser.read(newApi);

      OrderedJSONObject oldSwaggerJson = new OrderedJSONObject(new String(Files.readAllBytes(Paths.get(oldApi)), StandardCharsets.UTF_8));
      String oldSwaggerJsonOrdered = oldSwaggerJson.write(false);
      OrderedJSONObject newSwaggerJson = new OrderedJSONObject(new String(Files.readAllBytes(Paths.get(newApi)), StandardCharsets.UTF_8));
      String newSwaggerJsonOrdered = newSwaggerJson.write(false);

      File oldApiFile = File.createTempFile("oldApi", "json"), newApiFile = File.createTempFile("newApi", "json");

      //TODO: No need to write to file. Just compared ordered strings...
      Files.write(oldApiFile.toPath(), oldSwaggerJsonOrdered.getBytes());
      Files.write(newApiFile.toPath(), newSwaggerJsonOrdered.getBytes());
      /*
      TODO: outcommented for testing. un-outcomment
      oldApiFile.deleteOnExit();
      newApiFile.deleteOnExit();
      */
      getLog().info("oldApiFile path = " + oldApiFile.getAbsolutePath());
      getLog().info("newApiFile path = " + newApiFile.getAbsolutePath());
      OutputFormat.JSON.write(oldApiAsOpenApi, oldApiFile, true);
      OutputFormat.JSON.write(newApiAsOpenApi, newApiFile, true);
      //pretty print swagger object and compare string or file
      //TODO: Extract json file from war/jar OR use plugin
      return !FileUtils.contentEquals(oldApiFile, newApiFile);
    } catch (IOException | JSONException e) {
      throw new MojoExecutionException(String.format("Exception when trying to compare content of downloaded swagger doc (%s) with generated swagger doc (%s)", oldApi, newApi), e);
    }
  }

  /**
   * Validate that
   * <ul>
   *   <li>the plugin has configured the goal generateSwaggerDoc
   *    * (which is mandatory as present plugin generates a client from the swagger.json doc assumed
   *    to have been generated by the generateSwaggerDoc goal</li>
   *    <li>the configured phase, if any, of present execution is either null (=default) or LifecyclePhase.DEPLOY
   * </ul>
   */
  private void validatePhaseAndGoals() {

    //System.out.println("LifeCyclePhase: " + execution.getLifecyclePhase());
  }

  private String generateClient() throws MojoExecutionException {
    ClientGeneratorInput clientGeneratorInput = ClientGeneratorInput.builder()
      .log(getLog())
      .propertiesReader(propertiesReader)
      .pathToSwaggerDoc(getPathToSwaggerDoc())
      .project(project)
      .mavenSession(mavenSession)
      .pluginManager(pluginManager)
      .generateClientConfig(generateClientConfig)
      .build();

    for (GenerateClientConfig.Language language: generateClientConfig.getLanguages()){
      switch (language){
        case JAVA:
          return new JavaClientGenerator().generateClient(clientGeneratorInput);
        case DART:
          return new DartClientGenerator().generateClient(clientGeneratorInput);
      }
    }
    return "";
  }

  private String getPathToSwaggerDoc() {
    return project.getBuild().getOutputDirectory()+"/swagger/swagger.json";
  }

  //https://maven.apache.org/shared/maven-invoker/usage.html
  private void installClient() throws MojoExecutionException {
    getLog().info("Installing client in local repository. TODO...");

    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile( new File( getClientPomFile() ) );
    request.setBaseDirectory( new File(ClientGenerator.getOutputPath( project)+"/java")); //TODO: make more generic. What about fx dart?

    //request.setGoals( Arrays.asList( "clean", "install" ) );
    String pathToRipMavenPluginJar = "";
    try {
      pathToRipMavenPluginJar = new File(GenerateClientMojo.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
      pathToRipMavenPluginJar.replace("\\", "/");
      getLog().info("rip-maven-plugin jar path: " + pathToRipMavenPluginJar);
    } catch (URISyntaxException e) {
      throw new MojoExecutionException(e);
    }
    //TODO: work in progress. Trying to compile WITH abstractprocessor
    request.setGoals( Arrays.asList( "clean", "install", "-DMAVEN_OPTS=\"-processorpath "+pathToRipMavenPluginJar+"\"\" ) );//path/to/mapstruct-processor-1.3.0.Beta1.jar\"" ) );
    MavenExecutionRequest mavenExecutionRequest = mavenSession.getRequest();
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
  }

  private String getClientPomFile(){
    String pathToClientPom = ClientGenerator.getOutputPath( project) +"/pom.xml";
    getLog().info("Path to pom.xml of generated client code: " + pathToClientPom);
    return pathToClientPom;
  }

  /**
   * http://techtraits.com/build%20management/maven/2012/06/24/Maven-Deploying-multiple-artifacts-from-one-build.html
   */
  private void deployClient() throws MojoExecutionException {
    getLog().info("Deploying client to binary repo.");
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
  }

}
