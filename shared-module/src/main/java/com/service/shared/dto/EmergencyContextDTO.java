package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Emergency Context
 * Represents an emergency situation that requires guaranteed connectivity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmergencyContextDTO {

    /**
     * Unique emergency event ID
     */
    private String emergencyId;

    /**
     * Device phone number in E.164 format
     */
    private String phoneNumber;

    /**
     * Device IMEI
     */
    private String deviceImei;

    /**
     * Emergency type: SOS_BUTTON, GEOFENCE, EXTERNAL_EVENT
     */
    private EmergencyType emergencyType;

    /**
     * Device role: AMBULANCE, POLICE, FIRE, HOSPITAL, OTHER
     */
    private DeviceRole deviceRole;

    /**
     * Emergency severity: CRITICAL, HIGH, MEDIUM, LOW
     */
    private EmergencySeverity severity;

    /**
     * Geographic location (latitude)
     */
    private Double latitude;

    /**
     * Geographic location (longitude)
     */
    private Double longitude;

    /**
     * Geofence ID if triggered by geofence
     */
    private String geofenceId;

    /**
     * External system event ID (if from city/hospital/police system)
     */
    private String externalEventId;

    /**
     * Timestamp when emergency was detected
     */
    private LocalDateTime detectedAt;

    /**
     * Timestamp when emergency was resolved
     */
    private LocalDateTime resolvedAt;

    /**
     * Current status: ACTIVE, RESOLVED, CANCELLED
     */
    private EmergencyStatus status;

    /**
     * Additional context/metadata
     */
    private String metadata;

    public enum EmergencyType {
        SOS_BUTTON,
        GEOFENCE,
        EXTERNAL_EVENT,
        MANUAL_TRIGGER
    }

    public enum DeviceRole {
        AMBULANCE,
        POLICE,
        FIRE,
        HOSPITAL,
        EMERGENCY_COMMAND,
        OTHER
    }

    public enum EmergencySeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum EmergencyStatus {
        ACTIVE,
        RESOLVED,
        CANCELLED
    }
}
