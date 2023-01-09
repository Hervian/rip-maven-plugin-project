package ${package};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Configuration
public class ClientApiConfig {

<#list restClients as restClient>
    @Value("${restClient.url.value}")
    private String ${restClient.url.varName};

</#list>

/*
Work in progress. Adding a default resilience setup is difficult, maybe impossible in the sense: What fallback does people want? What endpoints do they wish to Retry (certainly not non-idempotent ones...)
That said, uncommenting the resiliency related passages in this file AND adding an aspect for adding the resiliency features should do the job. A work in progress aspect is available in the meta-server project. It needs some brushing off.
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResilientApi{}
*/

<#list restClients as restClient>

    <#list restClient.apis as api>
      @Bean
      public ${api.apiFqcn} ${api.apiVarName}${restClient.getArtifactIdAsJavaTypeName()}() {
        <#if restClient.isWebClient()>
            org.springframework.web.reactive.function.client.WebClient.Builder builder = ${restClient.apiClientFqcn}.buildWebClientBuilder();
            builder.baseUrl(${restClient.url.varName});
            ${restClient.apiClientFqcn} apiClient = new ${restClient.apiClientFqcn}(builder.build());
            ${api.apiFqcn} ${api.apiVarName} = new ${api.apiFqcn}(apiClient);
            return ${api.apiVarName};
          }

          /*
          Work in progress. Adding a default resilience setup is difficult, maybe impossible in the sense: What fallback does people want? What endpoints do they wish to Retry (certainly not non-idempotent ones...)
          That said, uncommenting the resiliency related passages in this file AND adding an aspect for adding the resiliency features should do the job. A work in progress aspect is available in the meta-server project. It needs some brushing off.
          @ResilientApi
          public static class Resilient${api.simpleName} extends ${api.apiFqcn} {
            public Resilient${api.simpleName}(${restClient.apiClientFqcn} apiClient) {
              super(apiClient);
            }
          }

          @Bean
          public Resilient${api.simpleName} resilient${api.simpleName}() {
            org.springframework.web.reactive.function.client.WebClient.Builder builder = ${restClient.apiClientFqcn}.buildWebClientBuilder();
            builder.baseUrl(${restClient.url.varName});
            ${restClient.apiClientFqcn} apiClient = new ${restClient.apiClientFqcn}(builder.build());
          Resilient${api.simpleName} ${api.apiVarName} = new Resilient${api.simpleName}(apiClient);
            return ${api.apiVarName};
          }
          */
        <#elseif restClient.isFeignClient()>
            ${restClient.apiClientFqcn} apiClient = new ${restClient.apiClientFqcn}();
            apiClient.setBasePath(${restClient.url.varName});
            ${api.apiFqcn} ${api.apiVarName} = apiClient.buildClient(${api.apiFqcn}.class);
            return ${api.apiVarName};
          }
        <#elseif restClient.isNativeClient()>
            ${restClient.apiClientFqcn} apiClient = new ${restClient.apiClientFqcn}();
            apiClient.updateBaseUri(${restClient.url.varName});
            ${api.apiFqcn} ${api.apiVarName} = new ${api.apiFqcn}(apiClient);
            return ${api.apiVarName};
          }

          /*
          Work in progress. Adding a default resilience setup is difficult, maybe impossible in the sense: What fallback does people want? What endpoints do they wish to Retry (certainly not non-idempotent ones...)
          That said, uncommenting the resiliency related passages in this file AND adding an aspect for adding the resiliency features should do the job. A work in progress aspect is available in the meta-server project. It needs some brushing off.
          @ResilientApi
          public static class Resilient${api.simpleName} extends ${api.apiFqcn} {
            public Resilient${api.simpleName}(${restClient.apiClientFqcn} apiClient) {
              super(apiClient);
            }
          }

          @Bean
          public Resilient${api.simpleName} resilient${api.simpleName}() {
            ${restClient.apiClientFqcn} apiClient = new ${restClient.apiClientFqcn}();
            apiClient.updateBaseUri(${restClient.url.varName});
            Resilient${api.simpleName} ${api.apiVarName} = new Resilient${api.simpleName}(apiClient);
            return ${api.apiVarName};
          }
          */
        </#if>

    </#list>

  @Service
  public static class ${restClient.getArtifactIdAsJavaTypeName()} implements RestClient {
    <#list restClient.apis as api>
      @Autowired
      private ${api.apiFqcn} ${api.apiVarName}${restClient.getArtifactIdAsJavaTypeName()};

      public ${api.apiFqcn} ${api.apiVarName}() {
        return ${api.apiVarName}${restClient.getArtifactIdAsJavaTypeName()};
      }
    </#list>

      @Override
      public String getGroupId() {
        return "${restClient.groupId}";
      }

      @Override
      public String getArtifactId() {
        return "${restClient.artifactId}";
      }

      @Override
      public String getVersion() {
        return "${restClient.version}";
      }

      @Override
      public List<Object> getApis() {
        return Arrays.asList(
          <#list restClient.apis as api>
              ${api.apiVarName}${restClient.getArtifactIdAsJavaTypeName()}<#if api_has_next>,</#if>
          </#list>
        );
      }
  }
</#list>


  public interface RestClient {
    String getGroupId();
    String getArtifactId();
    String getVersion();
    List<Object> getApis();
  }

}
