package com.api.jira.apis.task.model;

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
}
