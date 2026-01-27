package com.service.shared.exception;

import com.service.shared.dto.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for all microservices
 * Handles all exceptions and returns GlobalResponse format
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.failureWithData(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                ));
    }

    /**
     * Handle GlobalException (custom service exceptions)
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<GlobalResponse> handleGlobalException(
            GlobalException ex, WebRequest request) {
        log.error("Global exception: [{}] {}", ex.getErrorCode(), ex.getErrorMessage(), ex);
        return ResponseEntity.status(ex.getErrorCode())
                .body(GlobalResponse.failure(
                        ex.getErrorCode(),
                        ex.getErrorMessage()
                ));
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                ));
    }

    /**
     * Handle RuntimeException (general runtime exceptions)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
                ));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred. Please try again later."
                ));
    }
}
