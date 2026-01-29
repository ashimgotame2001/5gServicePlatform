package com.service.shared.service.impl;

import com.service.shared.dto.EmergencyContextDTO;
import com.service.shared.dto.EmergencyEventDTO;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.service.EmergencyContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of Emergency Context Service
 * Handles BR-1: Emergency Context Detection
 */
@Slf4j
@Service
public class EmergencyContextServiceImpl implements EmergencyContextService {

    // In-memory storage for emergency contexts (should be replaced with database in production)
    private final Map<String, EmergencyContextDTO> emergencyContexts = new ConcurrentHashMap<>();
    private final Map<String, List<EmergencyContextDTO>> emergenciesByPhoneNumber = new ConcurrentHashMap<>();

    @Override
    public GlobalResponse detectEmergencyFromGeofence(String phoneNumber, String geofenceId,
                                                       Double latitude, Double longitude) {
        log.info("Detecting emergency from geofence for phone: {}, geofence: {}", phoneNumber, geofenceId);
        
        String emergencyId = UUID.randomUUID().toString();
        EmergencyContextDTO context = EmergencyContextDTO.builder()
                .emergencyId(emergencyId)
                .phoneNumber(phoneNumber)
                .emergencyType(EmergencyContextDTO.EmergencyType.GEOFENCE)
                .severity(EmergencyContextDTO.EmergencySeverity.HIGH)
                .latitude(latitude)
                .longitude(longitude)
                .geofenceId(geofenceId)
                .detectedAt(LocalDateTime.now())
                .status(EmergencyContextDTO.EmergencyStatus.ACTIVE)
                .build();
        
        emergencyContexts.put(emergencyId, context);
        emergenciesByPhoneNumber.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(context);
        
        return GlobalResponse.successWithData(200, "Emergency context created from geofence", context);
    }

    @Override
    public GlobalResponse detectEmergencyFromSOS(String phoneNumber, String deviceImei,
                                                  Double latitude, Double longitude) {
        log.info("Detecting emergency from SOS for phone: {}", phoneNumber);
        
        String emergencyId = UUID.randomUUID().toString();
        EmergencyContextDTO context = EmergencyContextDTO.builder()
                .emergencyId(emergencyId)
                .phoneNumber(phoneNumber)
                .deviceImei(deviceImei)
                .emergencyType(EmergencyContextDTO.EmergencyType.SOS_BUTTON)
                .severity(EmergencyContextDTO.EmergencySeverity.CRITICAL)
                .latitude(latitude)
                .longitude(longitude)
                .detectedAt(LocalDateTime.now())
                .status(EmergencyContextDTO.EmergencyStatus.ACTIVE)
                .build();
        
        emergencyContexts.put(emergencyId, context);
        emergenciesByPhoneNumber.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(context);
        
        return GlobalResponse.successWithData(200, "Emergency context created from SOS", context);
    }

    @Override
    public GlobalResponse detectEmergencyFromExternalEvent(String externalEventId,
                                                           EmergencyContextDTO.DeviceRole deviceRole,
                                                           EmergencyContextDTO.EmergencySeverity severity,
                                                           Double latitude, Double longitude) {
        log.info("Detecting emergency from external event: {}", externalEventId);
        
        String emergencyId = UUID.randomUUID().toString();
        EmergencyContextDTO context = EmergencyContextDTO.builder()
                .emergencyId(emergencyId)
                .emergencyType(EmergencyContextDTO.EmergencyType.EXTERNAL_EVENT)
                .deviceRole(deviceRole)
                .severity(severity)
                .latitude(latitude)
                .longitude(longitude)
                .externalEventId(externalEventId)
                .detectedAt(LocalDateTime.now())
                .status(EmergencyContextDTO.EmergencyStatus.ACTIVE)
                .build();
        
        emergencyContexts.put(emergencyId, context);
        
        return GlobalResponse.successWithData(200, "Emergency context created from external event", context);
    }

    @Override
    public GlobalResponse getEmergencyContext(String emergencyId) {
        EmergencyContextDTO context = emergencyContexts.get(emergencyId);
        if (context == null) {
            return GlobalResponse.failure(404, "Emergency context not found: " + emergencyId);
        }
        return GlobalResponse.successWithData(200, "Emergency context retrieved", context);
    }

    @Override
    public GlobalResponse getActiveEmergenciesByPhoneNumber(String phoneNumber) {
        List<EmergencyContextDTO> activeEmergencies = emergenciesByPhoneNumber.getOrDefault(phoneNumber, new ArrayList<>())
                .stream()
                .filter(e -> e.getStatus() == EmergencyContextDTO.EmergencyStatus.ACTIVE)
                .toList();
        
        return GlobalResponse.successWithData(200, "Active emergencies retrieved", activeEmergencies);
    }

    @Override
    public GlobalResponse resolveEmergency(String emergencyId) {
        EmergencyContextDTO context = emergencyContexts.get(emergencyId);
        if (context == null) {
            return GlobalResponse.failure(404, "Emergency context not found: " + emergencyId);
        }
        
        context.setStatus(EmergencyContextDTO.EmergencyStatus.RESOLVED);
        context.setResolvedAt(LocalDateTime.now());
        
        return GlobalResponse.successWithData(200, "Emergency resolved", context);
    }

    @Override
    public GlobalResponse cancelEmergency(String emergencyId, String reason) {
        EmergencyContextDTO context = emergencyContexts.get(emergencyId);
        if (context == null) {
            return GlobalResponse.failure(404, "Emergency context not found: " + emergencyId);
        }
        
        context.setStatus(EmergencyContextDTO.EmergencyStatus.CANCELLED);
        context.setResolvedAt(LocalDateTime.now());
        context.setMetadata(reason);
        
        return GlobalResponse.successWithData(200, "Emergency cancelled", context);
    }

    @Override
    public EmergencyEventDTO createEmergencyEvent(EmergencyContextDTO context,
                                                   EmergencyEventDTO.EventType eventType) {
        return EmergencyEventDTO.builder()
                .eventId(UUID.randomUUID().toString())
                .emergencyContext(context)
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .sourceService("emergency-context-service")
                .correlationId(context.getEmergencyId())
                .build();
    }
}
