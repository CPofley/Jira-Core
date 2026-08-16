package com.api.jira.apis.task.model;


import lombok.Builder;
import lombok.Data;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateEventDto {
    private Integer taskId;
    private String updatedBy;
    private Map<String, Object> fields;
    private String type;
}
