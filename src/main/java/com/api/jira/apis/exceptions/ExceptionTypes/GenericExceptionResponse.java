package com.api.jira.apis.exceptions.ExceptionTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericExceptionResponse {
    private String error;
    Map<String,String> fieldErrors;
}
