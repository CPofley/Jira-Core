package com.api.jira.apis.exceptions.ExceptionTypes;


public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(String error){
        super(error);
    }
}
