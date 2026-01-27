package com.service.shared.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document to store API request/response logs
 */
@Document(collection = "api_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLog {

    @Id
    private String id;

    /**
     * Unique method code (e.g., "CS001" for createSession)
     */
    private String methodCode;

    /**
     * Method name (e.g., "createSession")
     */
    private String methodName;

    /**
     * Method description from annotation
     */
    private String methodDescription;

    /**
     * HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    private String httpMethod;

    /**
     * Request URL
     */
    private String url;

    /**
     * Request body as JSON string
     */
    private String requestBody;

    /**
     * Response body as JSON string
     */
    private String responseBody;

    /**
     * HTTP status code
     */
    private Integer statusCode;

    /**
     * Request headers as JSON string
     */
    private String requestHeaders;

    /**
     * Response headers as JSON string
     */
    private String responseHeaders;

    /**
     * Execution time in milliseconds
     */
    private Long executionTime;

    /**
     * Error message if any
     */
    private String errorMessage;

    /**
     * Exception stack trace if any
     */
    private String stackTrace;

    /**
     * Timestamp when the request was received
     */
    private LocalDateTime requestTimestamp;

    /**
     * Timestamp when the response was sent
     */
    private LocalDateTime responseTimestamp;

    /**
     * IP address of the client
     */
    private String clientIp;

    /**
     * User agent
     */
    private String userAgent;
}
