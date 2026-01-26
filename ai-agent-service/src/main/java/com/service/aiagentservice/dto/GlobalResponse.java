package com.service.aiagentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> GlobalResponse<T> success(String code, String message, T data) {
        return GlobalResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> GlobalResponse<T> success(String message, T data) {
        return success("SUCCESS", message, data);
    }

    public static <T> GlobalResponse<T> success(String message) {
        return success("SUCCESS", message, null);
    }

    public static <T> GlobalResponse<T> error(String code, String message) {
        return GlobalResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> GlobalResponse<T> error(String message) {
        return error("ERROR", message);
    }
}
