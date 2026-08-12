package com.api.jira.apis.exceptions.ExceptionTypes;
public class TaskTemplateException extends RuntimeException{
    public TaskTemplateException(String error){
        super(error);
    }
}
