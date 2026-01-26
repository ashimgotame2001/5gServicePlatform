package com.service.apigateway.util;

import com.service.apigateway.dto.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHelper {

    public static <T> ResponseEntity<GlobalResponse<T>> successWithData(String message, T data) {
        return ResponseEntity.ok(GlobalResponse.successWithData(
                HttpStatus.OK.value(),
                message,
                data
        ));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> successWithData(
            HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.successWithData(
                        status.value(),
                        message,
                        data
                ));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> successWithoutData(String message) {
        return ResponseEntity.ok(GlobalResponse.successWithoutData(
                HttpStatus.OK.value(),
                message
        ));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> successWithoutData(
            HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.successWithoutData(
                        status.value(),
                        message
                ));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> failure(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.failure(
                        status.value(),
                        message
                ));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> failureWithData(
            HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.failureWithData(
                        status.value(),
                        message,
                        data
                ));
    }
}
