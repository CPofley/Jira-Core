package com.api.jira.apis.exceptions.ExceptionTypes;

public class EmailFormatException extends RuntimeException{
    public EmailFormatException(String error){
        super(error);
    }
}
