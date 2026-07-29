package com.api.jira.apis.comment.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Integer id;
    private String comment;
    private String author;
    private Integer taskId;
    private LocalDateTime timestamp;
    private boolean updated;
}
