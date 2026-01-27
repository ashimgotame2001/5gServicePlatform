package com.service.shared.exception;

/**
 * Custom global exception for microservices
 * Used for handling service-specific errors with proper error codes and messages
 */
public class GlobalException extends RuntimeException {

    private final Integer errorCode;
    private final String errorMessage;

    public GlobalException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public GlobalException(Integer errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
