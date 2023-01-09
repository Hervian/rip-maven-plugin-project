package ${package};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Rest {

  //The Rest class consists of static method and members. To allow injection of the http client beans we use below trick
  @Component
  public static class BeanInjector {
    <#list restClients as restClient>
    @Autowired
    private ClientApiConfig.${restClient.getArtifactIdAsJavaTypeName()} _${restClient.getArtifactIdAsJavaTypeName()};
    </#list>
    @PostConstruct
    public void postConstruct() {
      <#list restClients as restClient>
      Rest._${restClient.getArtifactIdAsJavaTypeName()} = _${restClient.getArtifactIdAsJavaTypeName()};
      </#list>
    }
  }

  <#list restClients as restClient>
  private static ClientApiConfig.${restClient.getArtifactIdAsJavaTypeName()} _${restClient.getArtifactIdAsJavaTypeName()};

  public static ClientApiConfig.${restClient.getArtifactIdAsJavaTypeName()} ${restClient.getArtifactIdAsJavaGetter()}() {
    return _${restClient.getArtifactIdAsJavaTypeName()};
  }
  </#list>

}
