package com.api.jira.apis.task.model;

import com.api.jira.apis.comment.models.CommentDto;
import com.api.jira.apis.user.entity.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class TaskUpdateEventDto {
    private Integer id;
    private String title;
    private String description;
    private TaskType taskType;
    private String assignee;
    private String reporter;
    private TaskStatus taskStatus;
    private Priority priority;
    private LocalDateTime createdAt;
    private Set<CommentDto> comments;
    private ParentTaskDto parentTask;
    private Set<ChildTasksDto> subIssues;
    private UserEntity updatedBy;
    private LocalDateTime updatedAt;
}
