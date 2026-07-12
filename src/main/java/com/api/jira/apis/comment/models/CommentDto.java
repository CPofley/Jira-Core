package com.api.jira.apis.comment.models;

import lombok.Data;

@Data
public class CommentDto {
    private Integer id;
    private String comment;
    private String author;
    private Integer taskId;
}
