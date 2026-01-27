package com.service.shared.util;

import com.service.shared.dto.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Helper class for creating standardized responses
 * Provides methods for success and failure responses
 */
public class ResponseHelper {

    /**
     * Create success response with data
     * @param message Success message
     * @param data Response data
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> successWithData(String message, Object data) {
        return ResponseEntity.ok(GlobalResponse.successWithData(
                HttpStatus.OK.value(),
                message,
                data
        ));
    }

    /**
     * Create success response with data and custom status code
     * @param status HTTP status code
     * @param message Success message
     * @param data Response data
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> successWithData(
            HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.successWithData(
                        status.value(),
                        message,
                        data
                ));
    }

    /**
     * Create success response without data
     * @param message Success message
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> successWithoutData(String message) {
        return ResponseEntity.ok(GlobalResponse.successWithoutData(
                HttpStatus.OK.value(),
                message
        ));
    }

    /**
     * Create success response without data with custom status code
     * @param status HTTP status code
     * @param message Success message
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> successWithoutData(
            HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.successWithoutData(
                        status.value(),
                        message
                ));
    }

    /**
     * Create failure response
     * @param status HTTP status code
     * @param message Error message
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> failure(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.failure(
                        status.value(),
                        message
                ));
    }

    /**
     * Create failure response with data
     * @param status HTTP status code
     * @param message Error message
     * @param data Error data (e.g., validation errors)
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> failureWithData(
            HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status)
                .body(GlobalResponse.failureWithData(
                        status.value(),
                        message,
                        data
                ));
    }

    /**
     * Create not found response
     * @param message Error message
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> notFound(String message) {
        return failure(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Create bad request response
     * @param message Error message
     * @return ResponseEntity with GlobalResponse
     */
    public static ResponseEntity<GlobalResponse> badRequest(String message) {
        return failure(HttpStatus.BAD_REQUEST, message);
    }
}
