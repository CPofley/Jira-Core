package com.api.jira.apis.task.model;

import java.util.Arrays;
import java.util.List;

public enum TaskStatus {
    TO_DO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done"),
    RE_OPENED("Reopened"),
    NOT_REQUIRED("Not Required"),
    BLOCKED("Blocked");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<String> enumStrings(){
        return Arrays.stream(values()).map(TaskStatus::getDisplayName).toList();
    }
}
