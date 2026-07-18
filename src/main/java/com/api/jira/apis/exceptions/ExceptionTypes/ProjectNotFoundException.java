package com.api.jira.apis.exceptions.ExceptionTypes;

public class ProjectNotFoundException extends RuntimeException{

    public ProjectNotFoundException(String message) {
        super(message);
    }
}
