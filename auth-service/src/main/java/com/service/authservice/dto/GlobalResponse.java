package com.service.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Global Response class used for both success and error responses
 * Code and message are mandatory fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {
    
    /**
     * Response code (mandatory)
     * Success codes: 200, 201, etc.
     * Error codes: 400, 401, 404, 500, etc.
     */
    private Integer code;
    
    /**
     * Response message (mandatory)
     * Descriptive message about the operation result
     */
    private String message;
    
    /**
     * Response data (optional)
     * Contains the actual response payload
     */
    private T data;
    
    /**
     * Timestamp of the response
     */
    private Long timestamp;
    
    /**
     * Helper method to create success response with data
     */
    public static <T> GlobalResponse<T> successWithData(Integer code, String message, T data) {
        return GlobalResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Helper method to create success response without data
     */
    public static <T> GlobalResponse<T> successWithoutData(Integer code, String message) {
        return GlobalResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Helper method to create failure response
     */
    public static <T> GlobalResponse<T> failure(Integer code, String message) {
        return GlobalResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Helper method to create failure response with data
     */
    public static <T> GlobalResponse<T> failureWithData(Integer code, String message, T data) {
        return GlobalResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
