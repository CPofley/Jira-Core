package com.api.jira.apis.task.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class CreateSubTaskRequest {
    @NotNull(message = "Title is required")
    private String title;
    @NotNull(message = "Task Type required")
    private TaskType taskType;
    @NotNull(message = "Current Task ID is required")
    private Integer currentTaskId;
}
