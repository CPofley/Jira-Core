package com.api.jira.apis.task.model;

import lombok.Data;

@Data
public class ChildTasksDto {
    private Integer id;
    private String title;
    private String taskType;
    private String taskStatus;
}
