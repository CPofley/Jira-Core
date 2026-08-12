package com.api.jira.apis.task.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TaskType {
    STORY("User Story"),
    BUG("Bug"),
    TASK("Task"),
    EPIC("Epic"),
    SUB_TASK("Sub-task");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<String> enumStrings(){
        return Arrays.stream(values()).map(TaskType::getDisplayName).collect(Collectors.toList());
    }
}
