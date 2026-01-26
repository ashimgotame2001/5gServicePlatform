package com.service.aiagentservice.exception;

import com.service.aiagentservice.dto.GlobalResponse;
import com.service.aiagentservice.util.ResponseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument exception: {}", ex.getMessage(), ex);
        return ResponseHelper.error("INVALID_ARGUMENT", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GlobalResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state exception: {}", ex.getMessage(), ex);
        return ResponseHelper.error("INVALID_STATE", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation exception: {}", errors);
        return ResponseHelper.error("VALIDATION_ERROR", "Validation failed", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GlobalResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication exception: {}", ex.getMessage(), ex);
        return ResponseHelper.unauthorized("Authentication failed: " + ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied exception: {}", ex.getMessage(), ex);
        return ResponseHelper.error("ACCESS_DENIED", "Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<GlobalResponse<Object>> handleHttpClientErrorException(HttpClientErrorException ex) {
        log.error("HTTP client error: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        return ResponseHelper.error("CLIENT_ERROR", 
                "External service error: " + ex.getMessage(), 
                HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<GlobalResponse<Object>> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error("HTTP server error: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        return ResponseHelper.internalError("External service error: " + ex.getMessage());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<GlobalResponse<Object>> handleResourceAccessException(ResourceAccessException ex) {
        log.error("Resource access exception: {}", ex.getMessage(), ex);
        return ResponseHelper.error("SERVICE_UNAVAILABLE", 
                "Service unavailable: " + ex.getMessage(), 
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseHelper.internalError("An unexpected error occurred: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseHelper.internalError("An unexpected error occurred");
    }
}
