package com.github.hervian.swagger.mojos;

import com.github.hervian.swagger.compilation.ClassFileCopier;
import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.GenerateDocConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import com.github.hervian.swagger.generators.docs.DocumentGeneratorInput;
import com.github.hervian.swagger.generators.docs.JaxRsApiDocumentor;
import com.github.hervian.swagger.generators.docs.OpenApiDocumentGenerator;
import com.github.hervian.swagger.generators.docs.SpringWebfluxApiDocumentor;
import com.github.hervian.swagger.services.OpenApiConfig;
import com.github.hervian.swagger.util.MojoExecutorWrapper;
import com.google.common.collect.Lists;
import io.swagger.util.Yaml;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
@Mojo(name = "generateDoc",
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class GenerateDocMojo extends AbstractMojo {

  private PropertiesReader propertiesReader;

  public GenerateDocMojo() throws MojoExecutionException {
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
  private GenerateClientConfig generateClientConfig;

  private static final String openapiDocFileName = "swagger.json";

  List<String> listOfGoals;

  @Override
  public void execute() throws MojoExecutionException {
    getLog().info("Executing GenerateDocMojo (generates swagger.json (using io.openapitools.swagger:swagger-maven-plugin), additional docs as configured and a jax-rs annotated class to server the swagger.json doc etc.)");
    validatePhaseAndGoals();

    //TODO: if (!openApiDefinitionAnnotationFound()) { Create AbstractProcessor that sets this value
    generateOpenApiConfigClass();
    
    getLog().info("generating swagger.json document");
    File swaggerFile = generateSwaggerDoc(); //duration: ca 5 seconds
    //enrichGeneratedSwaggerDoc(swaggerFile);
    getLog().info("copying generated swagger.json document to build output folder");
    copySwaggerDocToBuildOutputDir(openapiDocFileName); // duration: ca 0 seconds

    if (!generateDocConfig.isSkipCheckForBreakingChanges()){
      String packaging = project.getPackaging();
      System.out.println("packaging: "+ packaging);
      failBuildOnUnflaggedBreakingChanges();
    }

    if (generateDocConfig.getAdditionalDocs()!=null){
      for (GenerateDocConfig.AdditionalDoc additionalDoc: generateDocConfig.getAdditionalDocs()){
        if (GenerateDocConfig.AdditionalDoc.NONE!=additionalDoc){
          getLog().info("generating swagger." + additionalDoc.name()); //duration: ca 4 seconds doc type
          generateAdditionalDocsFromSwaggerJsonDoc(additionalDoc);
        }
      }
    }

    getLog().info("copying swagger.html document to build output dir");
    copySwaggerDocToBuildOutputDir("index.html", "swagger.html");

    getLog().info("generating SwaggerDocJaxRsResource class (i.e. a class with jax-rs annotated method that serves the swagger.json)");
    copySwaggerDocJaxRsResourceToBuildOutputDir(); //duration: ca 1 second

    //Below seems to fail in current test. May be necessary to add CDATA to generated html before invoking below conversion: https://stackoverflow.com/a/16303854/6095334
    //HtmlToPdf.builder().project(project).build().execute();
  }

  private void generateOpenApiConfigClass() throws MojoExecutionException {
    getLog().info("Copying OpenApiConfig class to target folder.");
    ClassFileCopier classFileCopier = ClassFileCopier.
      builder()
      .log(getLog())
      .project(project)
      .resourcePackages(generateDocConfig.getResourcePackagesWithFallback(project))
      .build();
    List<Class<?>> classesToIncludeInCompileProcess = Lists.newArrayList(OpenAPIDefinition.class, Info.class);
    classesToIncludeInCompileProcess.addAll(generateDocConfig.getRestAnnotationType().getRestAnnotationTypes());
    String sourceCodeWithCorrectedPackage = classFileCopier.getSourceCode(OpenApiConfig.class);
    sourceCodeWithCorrectedPackage = sourceCodeWithCorrectedPackage.replaceAll("project.artifactId", project.getArtifactId());
    sourceCodeWithCorrectedPackage = sourceCodeWithCorrectedPackage.replaceAll("project.version", project.getVersion());
    sourceCodeWithCorrectedPackage = sourceCodeWithCorrectedPackage.replaceAll("project.description", project.getDescription() + getClientInfo());
    classFileCopier.copyResourceToBuildOutputDir(OpenApiConfig.class.getSimpleName(), sourceCodeWithCorrectedPackage, classesToIncludeInCompileProcess);
  }

  /**
   * Does not work. Delete
   * @param dest
   * @throws MojoExecutionException
   */
  private void enrichGeneratedSwaggerDoc(File dest) throws MojoExecutionException {
    try {
      String swaggerDoc = org.apache.commons.io.FileUtils.readFileToString(dest, "UTF-8");
      System.out.println("********************** Swagger doc before enriching:");
      System.out.println(swaggerDoc);
/*      OpenAPIV3Parser parser = new OpenAPIV3Parser();
      OpenAPI swaggerDocAsOpenApi = parser.read(dest.getPath());
      String description = swaggerDocAsOpenApi.getInfo().getDescription();
      swaggerDocAsOpenApi.getInfo().setDescription(getClientInfo() + "\n" + description);*/
      String enrichedSwaggerDoc = swaggerDoc.replace("\"description\":\"", "\"description\":\""+getClientInfo());//Json.pretty(swaggerDocAsOpenApi);
//
      System.out.println("********************** Swagger doc after enriching:");
      System.out.println(enrichedSwaggerDoc);
      org.apache.commons.io.FileUtils.writeStringToFile(dest, enrichedSwaggerDoc, "UTF-8");
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }
  }

  private String getClientInfo() {
    StringBuilder clientInfo = new StringBuilder("");
    if (generateClientMojoIsActivated()) { //TODO: clientInfo swagger.json description should be dependent on which client languages plugin was configured to create.
      clientInfo.append("The following clients are available for this server:<br><br>");
      if (generateClientConfig.getLanguages().contains(GenerateClientConfig.Language.JAVA)){
        clientInfo.append("<em>JAVA:</em><br>");
        clientInfo.append("<code>");
        clientInfo.append("&lt;dependency&gt;<br>" +
          "&emsp;&lt;groupId&gt;eu.mansipi&lt;/groupId&gt;<br>" +
          "&emsp;&lt;artifactId&gt;random-facts-server-client&lt;/artifactId&gt;<br>" +
          "&emsp;&lt;!-- use RELEASE (recommended), LATEST (to include SNAPSHOTS) or project.version --&gt;<br>" +
          "&emsp;&lt;version&gt;RELEASE&lt;/version&gt;<br>" +
          "&lt;/dependency&gt;</code><br><br>" +
          "Parent pom providing dependency and plugin management for applications built with Maven");
      }
      /*clientInfo.append("The following clients are available:\"\n")
      .append("+\"").append("&lt;dependency&gt;").append("\"\n")
        .append("+\"").append("&lt;groupId&gt;").append(project.getGroupId()).append("&lt;/groupId&gt;").append("\"\n")
        .append("+\"").append("&lt;artifactId&gt;").append(project.getArtifactId()).append("-client").append("&lt;/artifactId&gt;").append("\"\n")
        .append("+\"").append("&lt;version&gt;").append(project.getVersion()).append("&lt;/version&gt;").append("\"\n")
        .append("+\"").append("&lt;/dependency&gt;");*/
    }
    String clientInfoString = clientInfo.toString().replaceAll("project.version", project.getVersion());
    getLog().info(clientInfoString);
    return clientInfoString;
  }

  private boolean generateClientMojoIsActivated() {
    return listOfGoals.contains("generateClient");
  }

//TODO: Clean up this method
  /**
   * Validate that
   * <ul>
   *   <li>the configured phase, if any, of present execution is either null (=default) or at least LifecyclePhase.COMPILE and at most LifecyclePhase.prepare-package
   * </ul>
   */
  private void validatePhaseAndGoals() throws MojoExecutionException {
    System.out.println("mavenSession.getGoals(): " +mavenSession.getGoals());

    Plugin swaggerPlugin = project.getPlugin(String.format("%s:%s", propertiesReader.getGroupId(), propertiesReader.getArtifactId()));
    for (PluginExecution pluginExecution: swaggerPlugin.getExecutions()){ //Consider using maven-replacer-plugin https://gist.github.com/4n3w/3365657
      listOfGoals = pluginExecution.getGoals();
      System.out.println("goals: " + listOfGoals);
      System.out.println("phases: " + pluginExecution.getPhase());
    }
    Xpp3Dom dom = (Xpp3Dom)swaggerPlugin.getConfiguration();
    System.out.println("dom.getAttributeNames().length: " + dom.getAttributeNames().length);
    for (String attributeName: dom.getAttributeNames()){
      System.out.println("plugin attributeName: " + attributeName);
    }
    for (Xpp3Dom child: dom.getChildren()){
      System.out.println(child.getName()+", " + child.getValue());
    }
  }

  private File generateSwaggerDoc() throws MojoExecutionException {
    DocumentGeneratorInput input = DocumentGeneratorInput
      .builder()
      .log(getLog())
      .mavenSession(mavenSession)
      .pluginManager(pluginManager)
      .project(project)
      .generateDocConfig(generateDocConfig)
      .outputDir(getSwaggerDocDir())
      .build();

    OpenApiDocumentGenerator openApiDocumentGenerator = isSpringWithJaxRsProject() ? new JaxRsApiDocumentor() : new SpringWebfluxApiDocumentor();
    openApiDocumentGenerator.generate(input);
    File swaggerFile = new File(getSwaggerDocDir()+"/" + openapiDocFileName);
    return swaggerFile;
  }

  private boolean isSpringWithJaxRsProject() {
    //TODO: Perhaps one could deduce this by inspecting the classpath of the project implementing this plugin? Via a call to project.getCompileClasspathElements()
    return generateDocConfig.getRestAnnotationType()== GenerateDocConfig.RestAnnotationType.SPRING_JAX_RS;
  }

  //TODO: Either copy-paste to jar/build artifacts or attach as a separate jar'ed artifact using https://maven.apache.org/plugins/maven-jar-plugin/examples/attached-jar.html
  private void generateAdditionalDocsFromSwaggerJsonDoc(GenerateDocConfig.AdditionalDoc additionalDoc) throws MojoExecutionException {
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.openapitools"),
        artifactId("openapi-generator-maven-plugin"),
        version("5.4.0") //TODO get values from PropertiesReader
      ),
      goal("generate"),
      configuration(
        element(name("inputSpec"), project.getBuild().getOutputDirectory()+"/swagger/swagger.json"),
        element(name("generatorName"), additionalDoc.name().toLowerCase()),
        element(name("output"), project.getBuild().getDirectory()+"/generated-resources/swagger") //Extract to pom and access via PropertiesReader
      ),
      executionEnvironment(
        project,
        mavenSession,
        pluginManager
      )
    );
    //project.addCompileSourceRoot(project.getBuild().getDirectory()+"/generated-sources/openapi");//TODO: Delete this line?
  }

  /**
   * Finally, copy all the generated resources over to the build output folder because
   * we run after the "process-resources" phase and Maven no longer handles the copying
   * itself in these late phases. Based on this answer: https://stackoverflow.com/a/49724374/6095334
   * @throws MojoExecutionException
   */
  private void copySwaggerDocToBuildOutputDir(String fileName) throws MojoExecutionException {
    copySwaggerDocToBuildOutputDir(fileName, fileName);
  }

  private void copySwaggerDocToBuildOutputDir(String fileName, String destinationFileName) throws MojoExecutionException {
    try {
      File swaggerFile = new File(getSwaggerDocDir()+"/" + fileName);
      File swaggerFileDestination = new File(project.getBuild().getOutputDirectory()+"/swagger/" + destinationFileName);

      FileUtils.copyFile(swaggerFile, swaggerFileDestination);
      getLog().info(String.format("copied from: %s to: %s", swaggerFile, swaggerFileDestination));
    }
    catch (IOException e) {
      throw new MojoExecutionException("Unable to copy generated resources to build output folder", e);
    }
  }

  private String getSwaggerDocDir(){
    return project.getBuild().getDirectory()+"/generated-resources/swagger";
  }

  private void copySwaggerDocJaxRsResourceToBuildOutputDir() throws MojoExecutionException {
    ClassFileCopier classFileCopier = ClassFileCopier.
      builder()
      .log(getLog())
      .project(project)
      .resourcePackages(generateDocConfig.getResourcePackagesWithFallback(project))
      .build();
    Class<?> restType = generateDocConfig.getRestAnnotationType().getSwaggerDocResource();
    String sourceCodeWithCorrectedPackage = classFileCopier.getSourceCode(restType);
    classFileCopier.copyResourceToBuildOutputDir(restType.getSimpleName(), sourceCodeWithCorrectedPackage, generateDocConfig.getRestAnnotationType().getRestAnnotationTypes());
  }

  /**
   * See if below changes (major version stuff) can be solved by plugin devs - https://github.com/redskap/swagger-brake-maven-plugin/issues/23
   * Call swagger-brake-maven-plugin to check for breaking changes (if skipCheckForBreakingChanges=false)
   * Break the build if all of the following is true:
   * <ol>
   *   <li>The flag skipCheckForBreakingChanges is false (default is false)</li>
   *   <li>The swagger-brake-maven-plugin reports a breaking change</li>
   *   <li>The pom version have not had its major version increased</li>
   * </ol>
   *
   * Sources: https://arnoldgalovics.com/introducing-swagger-brake/
   *
   * @throws MojoExecutionException
   */
  private void failBuildOnUnflaggedBreakingChanges() throws MojoExecutionException {
    /*AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.scan("com.zetcode");
    ctx.refresh();

    var bean = ctx.getBean(Application.class);
    bean.run();

    ctx.close();
    LatestArtifactVersionResolver latestArtifactVersionResolver =
    String getPreviousVersion = latestArtifactVersionResolver.resolve(options);*/
    getLog().info("Calling swagger-brake-maven-plugin to check if there are breaking API changes that have not been reflected in the pom's version (semantic versioning).");
    String releaseRepository = null, snapshotRepository = null;
    for (org.apache.maven.model.Repository repository: (List<org.apache.maven.model.Repository>)project.getRepositories()){
      System.out.println("repository:" + repository);
      if (repository.getReleases()!=null && repository.getReleases().isEnabled()){
        System.out.println(repository.getUrl());
        releaseRepository = repository.getUrl();
      }
      if (repository.getSnapshots()!=null && repository.getSnapshots().isEnabled()){
        System.out.println(repository.getUrl());
        snapshotRepository = repository.getUrl();
      }
    }
    System.out.println("Using packaging: " + project.getPackaging());
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("io.redskap"),
        artifactId("swagger-brake-maven-plugin"),
        version(propertiesReader.getSwaggerBrakeVersion())
      ),
      goal("check"),
      configuration(
        element(name("newApi"), project.getBuild().getOutputDirectory()+"/swagger/swagger.json"),
        element(name("mavenRepoUrl"), getReleasesRepo(project)),//releaseRepository),
        element(name("mavenSnapshotRepoUrl"), getSnapshotRepo(project))//snapshotRepository)
      ),
      executionEnvironment(
        project,
        mavenSession,
        pluginManager
      )
    );
  }

  public static String getReleasesRepo(MavenProject project){
    String releaseRepository = null;
    for (org.apache.maven.model.Repository repository: (List<org.apache.maven.model.Repository>)project.getRepositories()){
      if (repository.getReleases()!=null && repository.getReleases().isEnabled()){
        System.out.println(repository.getUrl());
        releaseRepository = repository.getUrl();
      }
    }
    return releaseRepository;
  }

  public static String getSnapshotRepo(MavenProject project){
    String snapshotRepository = null;
    for (org.apache.maven.model.Repository repository: (List<org.apache.maven.model.Repository>)project.getRepositories()){
      if (repository.getSnapshots()!=null && repository.getSnapshots().isEnabled()){
        System.out.println(repository.getUrl());
        snapshotRepository = repository.getUrl();
      }
    }
    return snapshotRepository;
  }


}
