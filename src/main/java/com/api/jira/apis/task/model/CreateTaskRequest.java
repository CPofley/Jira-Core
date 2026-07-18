package com.api.jira.apis.task.model;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateTaskRequest {

    @NotNull(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Task type is required")
    private TaskType taskType;
    @NotBlank(message = "Reporter is mandatory")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Reporter must be a strictly valid email address (e.g., user@domain.com)"
    )
    @Schema(example = "abc@example.com", format = "email")
    @NotBlank(message = "Assignee is mandatory")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Assignee must be a strictly valid email address (e.g., user@domain.com)"
    )
    private String assignee;
    @NotNull(message = "Reporter is required")
    @Schema(example = "abc@example.com", format = "email")
    @Email(message = "Invalid reporter email syntax")
    private String reporter;
    private TaskStatus taskStatus;
    private Priority priority;
    @NotNull(message = "Project ID is mandatory")
    private Integer projectId;
}
