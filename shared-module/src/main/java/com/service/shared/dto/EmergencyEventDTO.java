package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka Event DTO for Emergency Broadcasting
 * Published to Kafka for parallel consumption by multiple services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmergencyEventDTO {

    /**
     * Event ID (UUID)
     */
    private String eventId;

    /**
     * Event type: EMERGENCY_DETECTED, EMERGENCY_RESOLVED, EMERGENCY_UPDATED
     */
    private EventType eventType;

    /**
     * Emergency context data
     */
    private EmergencyContextDTO emergencyContext;

    /**
     * Timestamp when event was created
     */
    private LocalDateTime timestamp;

    /**
     * Source service that created the event
     */
    private String sourceService;

    /**
     * Correlation ID for tracking across services
     */
    private String correlationId;

    /**
     * Additional event metadata
     */
    private Map<String, Object> metadata;

    public enum EventType {
        EMERGENCY_DETECTED,
        EMERGENCY_RESOLVED,
        EMERGENCY_UPDATED,
        EMERGENCY_CANCELLED
    }
}
