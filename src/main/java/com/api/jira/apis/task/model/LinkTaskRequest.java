package com.api.jira.apis.task.model;

import lombok.Data;

@Data
public class LinkTaskRequest {
    private Integer currentTaskId;
    private Integer taskToLinkId;
}
