package com.github.hervian.swagger.config;

import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.hervian.swagger.services.SwaggerDocJaxRsResource;
import com.github.hervian.swagger.services.SwaggerDocSpringResource;
import com.github.hervian.swagger.services.SwaggerUiJaxRsResource;
import com.github.hervian.swagger.services.SwaggerUiSpringResource;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.Getter;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
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

  public enum AdditionalDoc { //https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/java/org/openapitools/codegen/languages
    NONE,
    HTML,
    HTML2,
    CWIKI //Confluence wiki docs
  }

  @Parameter(defaultValue = "HTML2")
  private List<AdditionalDoc> additionalDocs;

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
  private RestAnnotationType restAnnotationType = RestAnnotationType.SPRING;

  @Parameter(defaultValue = "false")
  private boolean skipCheckForBreakingChanges;

  /**
   * Only used for Spring projects which uses Springs annotations (and not Jax-RS annotations). See {@link #restAnnotationType}
   * TODO: the hardcodet port wont work very well if it is in use by another app. Instead, the {@link com.github.hervian.swagger.generators.docs.SpringWebfluxApiDocumentor} should set a random port and the property below should use it: https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/maven-plugin/examples/it-random-port.html
   */
  @Parameter(defaultValue = "/v3/api-docs") //Should match the implementing project's configured path: springdoc.api-docs.path=/v3/api-docs (default)
  private String apiDocsUrl = "/v3/api-docs";

}
