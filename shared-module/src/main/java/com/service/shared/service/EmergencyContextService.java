package com.service.shared.service;

import com.service.shared.dto.EmergencyContextDTO;
import com.service.shared.dto.EmergencyEventDTO;
import com.service.shared.dto.GlobalResponse;

/**
 * Service interface for Emergency Context Management
 * Handles BR-1: Emergency Context Detection
 */
public interface EmergencyContextService {

    /**
     * Detect and create emergency context from geofence trigger
     */
    GlobalResponse detectEmergencyFromGeofence(String phoneNumber, String geofenceId, 
                                               Double latitude, Double longitude);

    /**
     * Detect and create emergency context from SOS button trigger
     */
    GlobalResponse detectEmergencyFromSOS(String phoneNumber, String deviceImei, 
                                          Double latitude, Double longitude);

    /**
     * Detect and create emergency context from external system event
     */
    GlobalResponse detectEmergencyFromExternalEvent(String externalEventId, 
                                                    EmergencyContextDTO.DeviceRole deviceRole,
                                                    EmergencyContextDTO.EmergencySeverity severity,
                                                    Double latitude, Double longitude);

    /**
     * Get active emergency context by ID
     */
    GlobalResponse getEmergencyContext(String emergencyId);

    /**
     * Get active emergency contexts by phone number
     */
    GlobalResponse getActiveEmergenciesByPhoneNumber(String phoneNumber);

    /**
     * Resolve emergency context
     */
    GlobalResponse resolveEmergency(String emergencyId);

    /**
     * Cancel emergency context
     */
    GlobalResponse cancelEmergency(String emergencyId, String reason);

    /**
     * Create emergency event for broadcasting
     */
    EmergencyEventDTO createEmergencyEvent(EmergencyContextDTO context, 
                                           EmergencyEventDTO.EventType eventType);
}
