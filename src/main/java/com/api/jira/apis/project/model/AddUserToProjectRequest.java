package com.api.jira.apis.project.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddUserToProjectRequest {
    @NotNull(message = "Project ID is mandatory")
    private Integer projectId;
    @NotNull(message = "User ID is mandatory")
    private Integer userId;
}
