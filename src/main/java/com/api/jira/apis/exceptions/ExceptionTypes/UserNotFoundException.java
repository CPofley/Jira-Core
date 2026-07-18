package com.api.jira.apis.exceptions.ExceptionTypes;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String error){
        super(error);
    }
}
