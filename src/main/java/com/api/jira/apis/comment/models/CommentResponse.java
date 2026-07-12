package com.api.jira.apis.comment.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CommentResponse {
    private Integer id;
    private String comment;
    private String author;
    private Integer taskId;
    private boolean updated;
}
