package com.service.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayResponse<T> {
    
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> GatewayResponse<T> successWithData(Integer code, String message, T data) {
        return GatewayResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> GatewayResponse<T> successWithoutData(Integer code, String message) {
        return GatewayResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> GatewayResponse<T> failure(Integer code, String message) {
        return GatewayResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> GatewayResponse<T> failureWithData(Integer code, String message, T data) {
        return GatewayResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
