package com.api.jira.apis.task.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaskMetadataConfig {
    private List<TaskStatus> statuses;
    private List<Priority> priorities;
    private List<TaskType> taskTypes;
}
