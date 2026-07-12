package com.api.jira.apis.project.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotNull(message = "Project name required")
    private String projectName;
    @NotNull(message = "Project description required")
    private String projectDescription;
}
