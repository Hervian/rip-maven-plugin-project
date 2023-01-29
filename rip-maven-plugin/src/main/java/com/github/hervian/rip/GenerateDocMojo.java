package com.github.hervian.rip;

import com.github.hervian.rip.util.compilation.ClassFileCopier;
import com.github.hervian.rip.config.GenerateClientConfig;
import com.github.hervian.rip.config.GenerateDocConfig;
import com.github.hervian.rip.config.PropertiesReader;
import com.github.hervian.rip.doc.DocumentGeneratorInput;
import com.github.hervian.rip.doc.JaxRsApiDocumentor;
import com.github.hervian.rip.doc.OpenApiDocumentGenerator;
import com.github.hervian.rip.doc.RestEndpointCallingOpenApiDocumentGenerator;
import com.github.hervian.rip.doc.src.OpenApiConfig;
import com.github.hervian.rip.util.CodeScanner;
import com.github.hervian.rip.util.MojoExecutorWrapper;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.Data;
import lombok.Getter;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
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

  @Getter
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

  @Parameter
  private GenerateDocConfig generateDocConfig;

  @Parameter
  private GenerateClientConfig generateClientConfig;

  public static final String openapiDocFileName = "swagger.json";
  private static final String openApiDocPath = "swagger/";
  public static final String swagerJsonFilePath = openApiDocPath + openapiDocFileName;

  List<String> listOfGoals;

  @Override
  public void execute() throws MojoExecutionException {
    getLog().info("Executing GenerateDocMojo (generates swagger.json (using io.openapitools.swagger:swagger-maven-plugin), additional docs as configured and a jax-rs annotated class to server the swagger.json doc etc.)");
    validatePhaseAndGoals();

    if (!generateDocConfig.isSkipCreateOpenApiDefinition()) {
      List<String> openApiDefinitionAnnotatedClasses = CodeScanner.findOpenAPIDefinitionAnnotations(project);
      if (openApiDefinitionAnnotatedClasses.isEmpty()) {
        getLog().info("No OpenAPIDefinition annotations found. An @OpenAPIDefinition annotated class will be generated. The description, version, title etc will be taken from the pom file.");
        generateOpenApiConfigClass();
      } else {
        getLog().info("OpenAPIDefinition(s) found: "+String.join(",", openApiDefinitionAnnotatedClasses)+"\nSkipping generation of OpenAPIDefinition annotated class.");
      }
    } else {
      getLog().info("Skipping generation of OpenAPIDefinition annotated class since the GenerateDocConfig param isSkipCreateOpenApiDefinition is set to true.");
    }
    
    getLog().info("generating swagger.json document");
    File swaggerFile = generateSwaggerDoc();
    getLog().info("copying generated swagger.json document to build output folder");
    copySwaggerDocToBuildOutputDir();

    if (!generateDocConfig.isSkipCheckForBreakingChanges()){
      String packaging = project.getPackaging();
      System.out.println("packaging: "+ packaging);
      failBuildOnUnflaggedBreakingChanges();
    }

    if (generateDocConfig.getAdditionalDocs()!=null && !generateDocConfig.getAdditionalDocs().isEmpty()){
      for (GenerateDocConfig.AdditionalDoc additionalDoc: generateDocConfig.getAdditionalDocs()){
        if (GenerateDocConfig.AdditionalDoc.NONE!=additionalDoc){
          getLog().info("generating swagger." + additionalDoc.name()); //duration: ca 4 seconds doc type
          generateAdditionalDocsFromSwaggerJsonDoc(additionalDoc);

          getLog().info("copying swagger.html document to build output dir");
          copySwaggerDocToBuildOutputDir("index." + additionalDoc.getFileExtension(), "swagger." + additionalDoc.getFileExtension());
        }
      }
    }

    if (generateDocConfig.isSkipGenerationOfOpenApiResource()) {
      getLog().info("Skipping creation of swagger doc resource / endpoint as per configuration [generateDocConfig.isSkipGenerationOfOpenApiResource()="+generateDocConfig.isSkipGenerationOfOpenApiResource()+"]");
    } else {
      getLog().info("generating SwaggerDocJaxRsResource class (i.e. a class with jax-rs annotated method that serves the swagger.html etc)  [generateDocConfig.isSkipGenerationOfOpenApiResource()="+generateDocConfig.isSkipGenerationOfOpenApiResource()+"]");
      copySwaggerDocJaxRsResourceToBuildOutputDir(); //duration: ca 1 second
    }

    //Below seems to fail in current test. May be necessary to add CDATA to generated html before invoking below conversion: https://stackoverflow.com/a/16303854/6095334
    //HtmlToPdf.builder().project(project).build().execute();
  }

  @Deprecated
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

  private String getClientInfo() {
    StringBuilder clientInfo = new StringBuilder("");
    if (generateClientMojoIsActivated()) { //TODO: clientInfo swagger.json description should be dependent on which client languages plugin was configured to create.
      clientInfo.append("The following clients have been autogenerated for this server:<br><br>");
      for (GenerateClientConfig.Language language: generateClientConfig.getLanguages()) {
        clientInfo.append(language.name()).append("<br>");
      }
      clientInfo.append("<br>");
      if (generateClientConfig.getLanguages().contains(GenerateClientConfig.Language.JAVA)){
        clientInfo.append("<em>Maven coordinate for the java client:</em><br>");
        clientInfo.append("<code>");
        clientInfo.append("&lt;dependency&gt;<br>" +
          "&emsp;&lt;groupId&gt;project.groupId&lt;/groupId&gt;<br>" +
          "&emsp;&lt;artifactId&gt;project.artifactId-client&lt;/artifactId&gt;<br>" +
          "&emsp;&lt;!-- use RELEASE (recommended), LATEST (to include SNAPSHOTS) or project.version --&gt;<br>" +
          "&emsp;&lt;version&gt;RELEASE&lt;/version&gt;<br>" +
          "&lt;/dependency&gt;</code><br><br>" +
          "Parent pom providing dependency and plugin management for applications built with Maven");
      }
    }
    String clientInfoString = clientInfo.toString().replace("project.version", project.getVersion());
    clientInfoString = clientInfoString.replace("project.artifactId", project.getArtifactId());
    clientInfoString = clientInfoString.replace("project.groupId", project.getArtifactId());
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
      .propertiesReader(propertiesReader)
      .generateDocConfig(generateDocConfig)
      .outputDir(getSwaggerDocDir())
      .build();

    OpenApiDocumentGenerator openApiDocumentGenerator = isSpringWithJaxRsProject() ? new JaxRsApiDocumentor() : new RestEndpointCallingOpenApiDocumentGenerator();
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
        version(propertiesReader.getOrgOpenapitoolsVersion())
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
  }

  /**
   * Finally, copy all the generated resources over to the build output folder because
   * we run after the "process-resources" phase and Maven no longer handles the copying
   * itself in these late phases. Based on this answer: https://stackoverflow.com/a/49724374/6095334
   * @throws MojoExecutionException
   */
  private void copySwaggerDocToBuildOutputDir() throws MojoExecutionException {
    copySwaggerDocToBuildOutputDir(openapiDocFileName, openapiDocFileName);
  }

  private void copySwaggerDocToBuildOutputDir(String fileName, String destinationFileName) throws MojoExecutionException {
    try {
      File swaggerFile = new File(getSwaggerDocDir()+"/" + fileName);
      File swaggerFileDestination = new File(project.getBuild().getOutputDirectory()+"/" +openApiDocPath + destinationFileName);

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
    sourceCodeWithCorrectedPackage = sourceCodeWithCorrectedPackage.replace("${apiDocsUrl}", generateDocConfig.getApiDocsUrl());
    sourceCodeWithCorrectedPackage = sourceCodeWithCorrectedPackage.replace("${swagger.json.path}", GenerateDocMojo.swagerJsonFilePath);
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