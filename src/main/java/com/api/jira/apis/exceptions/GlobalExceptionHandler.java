package com.api.jira.apis.exceptions;


import com.api.jira.apis.exceptions.ExceptionTypes.GenericExceptionResponse;
import com.api.jira.apis.exceptions.ExceptionTypes.ProjectNotFoundException;
import com.api.jira.apis.exceptions.ExceptionTypes.TaskNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    /**
     * Default handler for all the exceptions throughout the application
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericExceptionResponse> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        // You can also return a custom response entity if needed
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericExceptionResponse.builder().error(ex.getMessage()).build());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<GenericExceptionResponse> handleTaskNotFoundException(TaskNotFoundException ex){
        log.error("Task not found with details: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericExceptionResponse.builder().error(ex.getMessage()).build());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<GenericExceptionResponse> handleProjectNotFoundException(ProjectNotFoundException ex){
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericExceptionResponse.builder().error(ex.getMessage()).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericExceptionResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("Validation error: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GenericExceptionResponse.builder().error(errorMessage).build());
    }
}
