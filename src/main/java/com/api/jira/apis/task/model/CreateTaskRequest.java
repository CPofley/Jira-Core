package com.api.jira.apis.task.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTaskRequest {

    @NotNull(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Task type is required")
    private TaskType taskType;
    private String assignee;
    @NotNull(message = "Reporter is required")
    private String reporter;
    private TaskStatus taskStatus;
    private Priority priority;
    @NotNull(message = "Created by is required")
    private String createdBy;
    @NotNull(message = "Project ID is mandatory")
    private Integer projectId;
}
