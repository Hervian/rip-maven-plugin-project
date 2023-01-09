package com.github.hervian.swagger.services;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = @Server(url="localhost:8080"), info = @Info( description = "project.description", title = "project.artifactId", version = "project.version"))
public class OpenApiConfig {
}
