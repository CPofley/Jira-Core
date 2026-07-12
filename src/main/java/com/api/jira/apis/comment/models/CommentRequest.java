package com.api.jira.apis.comment.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // 🛠️ Required by Jackson to instantiate the DTO on incoming requests
@AllArgsConstructor
public class CommentRequest {
    @NotNull(message = "Comment is required")
    @JsonProperty("comment")
    private String comment;

    @JsonProperty("author")
    private String author;

    @NotNull(message = "Task ID is required")
    @JsonProperty("taskId") // 🛠️ Forces Jackson to bind "taskId" perfectly from JS
    private Integer taskId;
}
