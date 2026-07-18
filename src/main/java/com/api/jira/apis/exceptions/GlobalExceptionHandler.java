package com.api.jira.apis.exceptions;


import com.api.jira.apis.exceptions.ExceptionTypes.GenericExceptionResponse;
import com.api.jira.apis.exceptions.ExceptionTypes.ProjectNotFoundException;
import com.api.jira.apis.exceptions.ExceptionTypes.TaskNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        String errorMessage = ex.getLocalizedMessage();
        log.error("Validation error: {}", errorMessage);
        if (ex.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {

            // Target class type (e.g., TaskType)
            String targetType = ife.getTargetType().getSimpleName();
            // The value the user provided (e.g., "STORY-NEW")
            Object invalidValue = ife.getValue();

            // Extract allowed Enum constants if it's an enum class
            if (ife.getTargetType().isEnum()) {
                Object[] enumConstants = ife.getTargetType().getEnumConstants();
                String allowedValues = java.util.Arrays.toString(enumConstants);

                errorMessage = String.format("Invalid value '%s' for type '%s'. Accepted values are: %s",
                        invalidValue, targetType, allowedValues);
            } else {
                errorMessage = String.format("Invalid value '%s' for type '%s'.", invalidValue, targetType);
            }
        } else {
            // Fallback for other JSON syntax errors (e.g., missing commas, malformed brackets)
            errorMessage = "Malformed JSON syntax or missing required fields.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GenericExceptionResponse.builder()
                        .error(errorMessage)
                        .build());
    }
}
