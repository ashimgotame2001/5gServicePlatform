package com.service.shared.dto;

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
public class GlobalResponse {

    private Integer code;

    private String message;
    
    private Object data;
    
    private Long timestamp;
    
    /**
     * Helper method to create success response with data
     */
    public static GlobalResponse successWithData(Integer code, String message, Object data) {
        return GlobalResponse.builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Helper method to create success response without data
     */
    public static GlobalResponse successWithoutData(Integer code, String message) {
        return GlobalResponse.builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Helper method to create failure response
     */
    public static GlobalResponse failure(Integer code, String message) {
        return GlobalResponse.builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Helper method to create failure response with data
     */
    public static GlobalResponse failureWithData(Integer code, String message, Object data) {
        return GlobalResponse.builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
