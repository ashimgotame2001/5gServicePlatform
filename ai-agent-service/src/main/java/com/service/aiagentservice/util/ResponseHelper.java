package com.service.aiagentservice.util;

import com.service.aiagentservice.dto.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {
    
    public static <T> ResponseEntity<GlobalResponse<T>> success(String code, String message, T data) {
        return ResponseEntity.ok(GlobalResponse.success(code, message, data));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(GlobalResponse.success(message, data));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> success(String message) {
        return ResponseEntity.ok(GlobalResponse.success(message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> successWithData(T data) {
        return ResponseEntity.ok(GlobalResponse.success("Operation successful", data));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> successWithoutData(String message) {
        return ResponseEntity.ok(GlobalResponse.success(message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> error(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(GlobalResponse.error(code, message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> error(String code, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.error(code, message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> error(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponse.error(message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GlobalResponse.error("NOT_FOUND", message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GlobalResponse.error("UNAUTHORIZED", message));
    }
    
    public static <T> ResponseEntity<GlobalResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponse.error("INTERNAL_ERROR", message));
    }
}
