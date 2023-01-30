package com.github.hervian.rip;

import com.github.hervian.rip.rest_client.Feign;
import com.github.hervian.rip.rest_client.NativeClient;
import com.github.hervian.rip.rest_client.RestClient;
import com.github.hervian.rip.rest_client.Webclient;
import com.github.hervian.rip.config.GenerateRestConfig;
import com.github.hervian.rip.config.GenerateDocConfig;
import com.github.hervian.rip.config.PropertiesReader;
import com.google.common.io.Files;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import lombok.Data;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.reflections.util.ClasspathHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//TODO: Extend the client classes with a resilient version. I.e. Rest.feignClient().resilientJokesApi();
//I.e. edit the
@Data
@Mojo(name = "generateRest",
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class GenerateRestMojo extends AbstractMojo {

  private PropertiesReader propertiesReader;

  public GenerateRestMojo() throws MojoExecutionException {
    try {
      propertiesReader = new PropertiesReader();
    } catch (IOException e) {
      throw new MojoExecutionException("Error when constructing PropertiesReader object used to read properties from pom file.", e);
    }
  }

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Parameter
  private GenerateDocConfig generateDocConfig;

  @Parameter(defaultValue = "")
  private GenerateRestConfig generateRestConfig = new GenerateRestConfig();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      List<RestClient> restClients = getRestClients();
      generateRestApisWrapper(restClients);
      //Generate Rest.class with methods a la Rest.jokesApi().getRandomJoke();
      // Create a Bean for each api.
      // add static method to Rest type
    } catch (DependencyResolutionRequiredException | IOException | XmlPullParserException e) {
      throw new MojoExecutionException(e);
    }
  }

  private List<RestClient> getRestClients() throws IOException, DependencyResolutionRequiredException, MojoExecutionException, XmlPullParserException {
    ClassLoader classLoader = getClassLoader(project);

    List<String> foundTypes;
    try (ScanResult scanResult = new ClassGraph().enableClassInfo().overrideClasspath(ClasspathHelper.forClassLoader(classLoader)).scan()) {
      foundTypes = scanResult.getAllClasses().getNames().stream().filter(e -> e.endsWith("Api") || e.endsWith("ApiClient")).collect(Collectors.toList());
    }

    //group found classes into map from jar to set of classes
    Map<String, Set<Class>> mapFromJarPathToListOfClasses = new HashMap<>();
    for (String fqcn: foundTypes){
      Class foundClass = null;
      if (!fqcn.startsWith("java") && (fqcn.endsWith(".ApiClient") || fqcn.endsWith("Api"))) {
        try {
          foundClass = Class.forName(fqcn, false, classLoader);
        } catch (ClassNotFoundException e) {
          System.out.println("Unable to instantiate '" + fqcn + "'");
          //ignore for now...
          continue;
        }
        if ( // is public class or public interface
          !foundClass.isAnnotation()
            && !foundClass.isEnum()
            && !foundClass.isAnonymousClass()
            && !foundClass.isLocalClass()
            && !foundClass.isMemberClass()
            && (foundClass.getModifiers() & Modifier.PUBLIC) != 0) {
          String jarName = foundClass.getProtectionDomain().getCodeSource().getLocation().getPath();
          Set<Class> classes = mapFromJarPathToListOfClasses.getOrDefault(jarName, new HashSet<>());
          classes.add(foundClass);
          mapFromJarPathToListOfClasses.put(jarName, classes);
        }
      }
    }

    List<RestClient> restClients = new ArrayList<>();

    //Inspect classes
    for (Map.Entry<String, Set<Class>> entry: mapFromJarPathToListOfClasses.entrySet()){
      ResourceList resourceList;
      try (ScanResult scanResult = new ClassGraph().enableClassInfo().overrideClasspath(entry.getKey()).scan()) {
        resourceList = scanResult.getResourcesWithLeafName("pom.xml");
        if (resourceList.size()==1){
          String content = resourceList.get(0).getContentAsString();
          if (content.contains("<organization>OpenAPITools.org") || content.contains("<artifactId>spring-boot-starter-webflux")){
            Class<?> apiClient = null;
            List<RestClient.Api> apis = new ArrayList<>();
            RestClient.Api.ApiBuilder apiBuilder = RestClient.Api.builder();
            for (Class<?> clazz: entry.getValue()) {
              if ("ApiClient".equalsIgnoreCase(clazz.getSimpleName())) {
                apiClient = clazz;
              }
              if (clazz.getSimpleName().endsWith("Api")) {
                apis.add(
                  RestClient.Api.builder()
                    .clazz(clazz)
                    .apiFqcn(clazz.getName())
                    .apiVarName(getVarName(clazz))
                    .simpleName(clazz.getSimpleName())
                  .build()
                );
              }
            }

            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new ByteArrayInputStream(content.getBytes()));
            RestClient.Url url = RestClient.Url
              .builder()
              .value("${" + model.getGroupId() + model.getArtifactId() + ".endpoint") // TODO: add default value such as to make eureka work out of the box? Or localhost
              .varName(model.getGroupId().replaceAll("[^A-Za-z]", "") + model.getArtifactId().replaceAll("[^A-Za-z]", "") + "Url")
              .build();

            if (content.contains("<organization>OpenAPITools.org")){
              if (content.contains("<groupId>io.github.openfeign")){
                restClients.add( //TODO: Change design. Use one class (=RestClient) with an enum to distinguish type.
                  Feign.builder()
                      .apiClientFqcn(apiClient.getName())
                      .apis(apis)
                      .groupId(model.getGroupId())
                      .artifactId(model.getArtifactId())
                      .version(model.getVersion())
                      .url(url)
                    .build()
                );
                continue;
              } else if (content.contains("<artifactId>spring-boot-starter-webflux")){
                restClients.add(
                  Webclient.builder()
                      .apiClientFqcn(apiClient.getName())
                      .apis(apis)
                      .groupId(model.getGroupId())
                      .artifactId(model.getArtifactId())
                      .version(model.getVersion())
                      .url(url)
                    .build());
                continue;
              }
              else {
                //assume this is the java 11 http client, i.e. what has been generated using the openapi library "native"
                restClients.add(
                    NativeClient.builder()
                        .apiClientFqcn(apiClient.getName())
                        .apis(apis)
                        .groupId(model.getGroupId())
                        .artifactId(model.getArtifactId())
                        .version(model.getVersion())
                        .url(url)
                      .build()
                );
              }
            }
          }
        }
      }
    }
    restClients.sort(Comparator.comparing(RestClient::getArtifactId));
    for (int i=0; i<restClients.size(); i++) {
      restClients.get(i).getUrl().setValue(restClients.get(i).getUrl().getValue() + ":http://localhost:" + (8080+i+1) + "}");
    }
    System.out.println("**** REST clients found in scan ****");
    restClients.forEach(e -> System.out.println(e.getClass().getSimpleName() + ":\n\tApiClient: " + e.getApiClientFqcn() + "\n\tapis: " + e.getApis().stream().map(e2 -> e2.getApiFqcn()).collect(Collectors.joining(","))));
    return restClients;
  }

  private String getVarName(Class<?> clazz) {
    String simpleName = clazz.getSimpleName();
    return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
  }

  protected final ClassLoader getClassLoader(MavenProject project) throws MojoExecutionException, DependencyResolutionRequiredException {
    final List<String> classpathElements = new ArrayList<>();
    classpathElements.addAll(project.getCompileClasspathElements());
    classpathElements.addAll(project.getRuntimeClasspathElements());
    classpathElements.add(project.getBuild().getOutputDirectory());
    final List<URL> projectClasspathList = new ArrayList<>();
    for (final String element : classpathElements) {
      try {
        URL url = new File(element).toURI().toURL();
        /* System.out.println(url);*/
        projectClasspathList.add(url);
      } catch (final MalformedURLException ex) {
        throw new MojoExecutionException(
          element + " is an invalid classpath element", ex
        );
      }
    }
    return new URLClassLoader(
      projectClasspathList.toArray(new URL[projectClasspathList.size()]),
      Thread.currentThread().getContextClassLoader()
    );
  }

  /**
   * package ...
   *
   * import ...
   * import ...
   *
   * public class Rest {
   *
   *   public static class JokesServerClient {
   *     public static JokesApi jokesApi() {
   *       return null;
   *     }
   *
   *     public static TimeApi timeApi() {
   *       return null;
   *     }
   *   }
   *
   *   public static class RandomFactsServerClient {
   *     public static FactsApi factsApi(){
   *       return null;
   *     }
   *   }
   * }
   * @param restClients
   */
  private void generateRestApisWrapper(List<RestClient> restClients) throws MojoExecutionException, IOException {
    /*ClassFileCopier classFileCopier = ClassFileCopier.
      builder()
      .log(getLog())
      .project(project)
      .resourcePackages(generateRestConfig==null || generateRestConfig.getLocation()==null ? generateDocConfig.getResourcePackagesWithFallback(project) : Arrays.asList(generateRestConfig.getLocation())) //TODO: put in some subpackage/other package than where the controllers are
      .build();*/

    String packageString = generateRestConfig==null || generateRestConfig.getLocation()==null ? generateDocConfig.getResourcePackagesWithFallback(project).get(0) + ".rest" : generateRestConfig.getLocation(); //TODO: put in some subpackage/other package than where the controllers are
    String targetFolder = generateRestConfig.getTargetFolder()==null ? project.getBuild().getDirectory() + "/generated-sources/rest" : generateRestConfig.getTargetFolder();

    Map<String, Object> templateData = new HashMap<>();
    //templateData.put("urls", createUrls(restClients));
    templateData.put("package", packageString);
    templateData.put("restClients", restClients);

    createClientApiClass(restClients, packageString, targetFolder, templateData);
    createRestApiClass(restClients, packageString, targetFolder, templateData);
  }

  private void createClientApiClass(List<RestClient> restClients, String packageString, String targetFolder, Map<String, Object> templateData) throws MojoExecutionException, IOException {
    String clientApiConfigClassSourceCode = createSource("ClientApiConfig.ftl" ,restClients, packageString, templateData);
    String fileName = "ClientApiConfig.java";

    writeSourceFile(packageString, targetFolder, clientApiConfigClassSourceCode, fileName);
  }

  private void createRestApiClass(List<RestClient> restClients, String packageString, String targetFolder, Map<String, Object> templateData) throws IOException {
    String clientApiConfigClassSourceCode = createSource("Rest.ftl", restClients, packageString, templateData);
    String fileName = "Rest.java";

    writeSourceFile(packageString, targetFolder, clientApiConfigClassSourceCode, fileName);
  }


  private void writeSourceFile(String packageString, String targetFolder, String clientApiConfigClassSourceCode, String fileName) throws IOException {
    String pathToFile = targetFolder + "/" + packageString.replace(".", "/");
    File dest = new File(pathToFile + "/" + fileName);
    getLog().info("Writing "+ fileName +" file to " + dest.getPath());
    Files.createParentDirs(dest);
    dest.createNewFile();
    try (PrintStream out = new PrintStream(new FileOutputStream(dest))) {
      out.print(clientApiConfigClassSourceCode);
    }
    project.addCompileSourceRoot(pathToFile);
  }

  /*private String createClientApiSource(List<RestClient> restClients, String packageOfClass, Map<String, Object> templateData) {
    String clientApiSourceCode = "Not generated";
    try {
      Configuration cfg = new Configuration(new Version("2.3.31"));
      cfg.setDefaultEncoding("UTF-8");

      cfg.setClassForTemplateLoading(this.getClass(), "/templates/");
      Template template = cfg.getTemplate("ClientApiConfig.ftl");

      try (StringWriter out = new StringWriter()) { //https://zetcode.com/java/freemarker/
        template.process(templateData, out);
        clientApiSourceCode = out.getBuffer().toString();
        System.out.println(clientApiSourceCode);
        out.flush();
      }
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
    return clientApiSourceCode;
  }*/

  private String createSource(String templateName, List<RestClient> restClients, String packageString, Map<String, Object> templateData) {
    String clientApiSourceCode = "Not generated";
    try {
      Configuration cfg = new Configuration(new Version("2.3.31"));
      cfg.setDefaultEncoding("UTF-8");

      cfg.setClassForTemplateLoading(this.getClass(), "/templates/");
      Template template = cfg.getTemplate(templateName);

      try (StringWriter out = new StringWriter()) { //https://zetcode.com/java/freemarker/
        template.process(templateData, out);
        clientApiSourceCode = out.getBuffer().toString();
        System.out.println(clientApiSourceCode);
        out.flush();
      }
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
    return clientApiSourceCode;
  }

}
