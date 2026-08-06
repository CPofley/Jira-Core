package com.api.jira.apis.task.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateTaskRequest {
    @NotNull(message = "Task ID cannot be null")
    private Integer taskId;
    private Map<String, Object> fields;
    @NotNull(message = "Email ID cannot be null")
    private String emailId;
}
