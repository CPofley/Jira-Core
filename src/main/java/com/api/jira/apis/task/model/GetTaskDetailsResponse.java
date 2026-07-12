package com.api.jira.apis.task.model;

import lombok.Data;

import java.util.List;

@Data
public class GetTaskDetailsResponse {
    private final TaskDto tasks;
}
