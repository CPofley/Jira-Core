package com.api.jira.apis.task.model;

import com.api.jira.apis.comment.models.CommentDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDto {
    private Integer id;
    private String title;
    private String description;
    private TaskType taskType;
    private String assignee;
    private String reporter;
    private TaskStatus taskStatus;
    private Priority priority;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<CommentDto> comments;
    private ParentTaskDto parentTask;
    private List<ChildTasksDto> subIssues;
}
