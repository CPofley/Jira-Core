package com.api.jira.apis.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Jira Custom Workspace API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SpringConfig {

    private static final Map<String,String> API_GROUPS = Map.ofEntries(
      Map.entry("Tasks-API", "/api/tasks/**"),
      Map.entry("Users-API",  "/api/users/**"),
      Map.entry("Projects-API",   "/api/projects/**"),
      Map.entry("Comments-API",   "/api/comments/**")
    );

    @Bean
    public List<GroupedOpenApi> groupedOpenApis() {
        return API_GROUPS.entrySet().stream()
                .map(entry -> GroupedOpenApi.builder()
                        .group(entry.getKey())
                        .pathsToMatch(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
