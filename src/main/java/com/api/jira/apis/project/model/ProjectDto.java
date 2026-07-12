package com.api.jira.apis.project.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectDto {
    private final String projectName;
    private final String projectDescription;
    private final Integer projectId;
}
