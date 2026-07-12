package com.api.jira.apis.comment.models;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UpdateCommentRequest {
    @NotNull(message = "Comment ID required")
    private Integer commentId;
    @NotNull(message = "Comment is required")
    private String comment;
}
