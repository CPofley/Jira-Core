package com.api.jira.apis.task.model;

import lombok.Data;

@Data
public class ParentTaskDto {
    private Integer id;
    private String title;
    private String taskType;
}
