package com.api.jira.apis.task.model;

import java.util.Arrays;
import java.util.List;

public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> enumStrings(){
        return Arrays.stream(values()).map(Priority::getDisplayName).toList();
    }
}
