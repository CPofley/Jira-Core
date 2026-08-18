package com.api.jira.apis.task.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;

@Data
public class KeywordSearchRequest {
    @NotNull(message = "projectId is required")
    private Integer projectId;
    @NotBlank(message = "Keyword cannot be empty")
    private String keyword;
    private Integer page = 0;
    private Integer size = 20;
    public PageRequest toPageable() {
        return PageRequest.of(page, size);
    }
}
