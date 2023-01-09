package com.github.hervian.rip.rest_client;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
public abstract class RestClient {

  private String groupId;
  private String artifactId;
  private String version;

  private String apiClientFqcn;
  private List<Api> apis;

  private Url url;

  public String getArtifactIdAsJavaTypeName() {
    String artifactIdCopy = Character.toUpperCase(artifactId.charAt(0)) + artifactId.substring(1);
    System.out.println("artifactIdCopy = " + artifactIdCopy);
    while (artifactIdCopy.indexOf('-')>0) {
      System.out.println("artifactIdCopy = " + artifactIdCopy);
      Character upperCasedChar = null;
      if (artifactIdCopy.length()>artifactIdCopy.indexOf('-')) {
        upperCasedChar = Character.toUpperCase(artifactIdCopy.charAt(artifactIdCopy.indexOf('-')+1));
      }
      String lastPartOfString = "";
      if (artifactIdCopy.length()>artifactIdCopy.indexOf('-')+1) {
        lastPartOfString = artifactIdCopy.substring(artifactIdCopy.indexOf('-')+2);
      }
      artifactIdCopy = artifactIdCopy.substring(0, artifactIdCopy.indexOf('-')) + upperCasedChar + lastPartOfString;
    }
    System.out.println("Returning artifactIdCopy = " + artifactIdCopy);
    return artifactIdCopy;
  }

  public String getArtifactIdAsJavaGetter() {
    String typeName = getArtifactIdAsJavaTypeName();
    return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
  }

  public boolean isWebClient() {
    return this.getClass()==Webclient.class;
  }

  public boolean isFeignClient() {
    return this.getClass()==Feign.class;
  }

  public boolean isNativeClient() {
    return this.getClass()==NativeClient.class;
  }

  @Builder
  @Data
  public static class Api {
    private Class<?> clazz;
    private String apiFqcn;
    private String apiVarName;//apiFqcn with lower cased first letter. TODO: plugin config can map the simplename to custom string to avoid api name clash.
    private String simpleName;
  }

  @Builder
  @Data
  public static class Url {
    private String value;
    private String varName;
  }

}
