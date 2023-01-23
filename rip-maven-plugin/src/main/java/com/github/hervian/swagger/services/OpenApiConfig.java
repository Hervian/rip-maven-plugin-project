package com.github.hervian.swagger.services;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * This OpenAPIDefinition annotated class is autogenerated by the rip-maven-plugin when it registers
 * that no OpenApiDefinition annotation exists in the project.
 * It is necessary since the open api document generating framework uses it.
 *
 * Instead of relying of this autogenerated OpenApiDefinition you can create your own.
 */
//Example including security: https://www.tabnine.com/code/java/classes/io.swagger.v3.oas.annotations.OpenAPIDefinition?snippet=5ce6c0d537d9ff0004edab0a
//guide: https://www.baeldung.com/spring-openapi-global-securityscheme
@OpenAPIDefinition(
    //servers = @Server(url="http://localhost:8080"), //Before upgrading spring version 'localhost:8080' worked. After port is necessary. Likely, a relative url (which is default) will work. TODO: Is logic in place to set proper port?!
    servers = @Server(url="/"),
    info = @Info( description = "project.description", title = "project.artifactId", version = "project.version"))
public class OpenApiConfig {
}
