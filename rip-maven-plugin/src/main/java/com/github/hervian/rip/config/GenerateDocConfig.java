package com.github.hervian.rip.config;

import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.hervian.rip.doc.src.SwaggerDocJaxRsResource;
import com.github.hervian.rip.doc.src.SwaggerDocSpringResource;
import com.github.hervian.rip.ui.src.SwaggerUiJaxRsResource;
import com.github.hervian.rip.ui.src.SwaggerUiSpringResource;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GenerateDocConfig {

  @Parameter(required = true)
  private List<String> resourcePackages;

  //TODO: Make the default resource package be semi-configurable (GROUPID.ARTIFACTID.CONFIGURED_PACKAGES)
  public List<String> getResourcePackagesWithFallback(MavenProject project){
    if (resourcePackages==null || resourcePackages.isEmpty() || resourcePackages.stream().filter(s -> s != null && !s.isEmpty()).count()<1){
      String defaultResourceLocation = project.getGroupId() + "." + project.getArtifactId() + ".controllers";
      defaultResourceLocation = defaultResourceLocation.replace("-", "_");
      return Collections.singletonList(defaultResourceLocation);
    }
    return resourcePackages;
  }

  //TODO: make it possible to configure a profile that is used to start up the server and get the swagger.json

  public enum AdditionalDoc { //https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/java/org/openapitools/codegen/languages
    NONE(""),
    HTML("html"),
    HTML2("html"),
    CWIKI("cwiki"); //Confluence wiki docs

    @Getter
    private String fileExtension;

    private AdditionalDoc(String fileExtension){
      this.fileExtension = fileExtension;
    }
  }

  @Parameter(defaultValue = "HTML2")
  private List<AdditionalDoc> additionalDocs;

  /**
   * Whether the generateDoc mojo should skip the creation of an endpoint that serves the swagger.json.
   */
  @Parameter(defaultValue = "false")
  private boolean skipGenerationOfOpenApiResource;

  /**
   * By default the generateDoc mojo will detect if the project has an @OpenAPIDefinition annotation.
   * If missing, one will be created with info from the pom.xml file.
   */
  @Parameter(defaultValue = "false")
  private boolean skipCreateOpenApiDefinition;

  public enum RestAnnotationType { //https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/java/org/openapitools/codegen/languages
    SPRING(SwaggerDocSpringResource.class, SwaggerUiSpringResource.class, Arrays.asList(ConditionalOnProperty.class, GetMapping.class, RestController.class, RequestMapping.class, Operation.class, GET.class, Mono.class, MultiValueMap.class, ObjectMapper.class,  Versioned.class, Value.class, EventListener.class, ApplicationReadyEvent.class)),
    SPRING_JAX_RS(SwaggerDocJaxRsResource.class, SwaggerUiJaxRsResource.class, Arrays.asList(GET.class, Path.class, Context.class, Component.class, ConditionalOnProperty.class, Operation.class, StreamUtils.class, Value.class, JsonDeserialize.class, EventListener.class, ApplicationReadyEvent.class));

    @Getter private Class<?> swaggerDocResource;
    @Getter private Class<?> swaggerUiResource;
    @Getter private List<Class<?>> restAnnotationTypes;

    RestAnnotationType(Class<?> swaggerDocResource, Class<?> swaggerUiResource, List<Class<?>> restAnnotationTypes){
      this.swaggerDocResource = swaggerDocResource;
      this.swaggerUiResource = swaggerUiResource;
      this.restAnnotationTypes = restAnnotationTypes;
    }
  }

  @Parameter(defaultValue = "SPRING")
  @Builder.Default
  private RestAnnotationType restAnnotationType = RestAnnotationType.SPRING;

  /**
   * Configure the path at which the openapi doc (swagger.json) can be downloadet. Default is "/v3/api-docs" which is the default path set by the springdoc project.
   */
  @Parameter(defaultValue = "/v3/api-docs") //Should match the implementing project's configured path: springdoc.api-docs.path=/v3/api-docs (default)
  @Builder.Default
  private String apiDocsUrl = "/v3/api-docs"; //TODO: rename to pathToOpenApiDoc since this is not a url

}
