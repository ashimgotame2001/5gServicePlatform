package com.service.decisionengineservice.agent.impl;

import com.service.decisionengineservice.agent.BaseAgent;
import com.service.decisionengineservice.agent.model.AgentAction;
import com.service.decisionengineservice.agent.model.AgentContext;
import com.service.decisionengineservice.agent.model.AgentResult;
import com.service.shared.dto.*;
import com.service.shared.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Emergency Connectivity Agent - Guarantees connectivity for emergency services
 * Compatible with Emergency Connectivity Setup (BR-1 through BR-8)
 */
@Slf4j
@Component
public class EmergencyConnectivityAgent extends BaseAgent {
    
    private final EmergencyContextService emergencyContextService;
    private final TrustValidationService trustValidationService;
    private final NetworkStateAssessmentService networkStateAssessmentService;
    private final EmergencyDecisionEngineService decisionEngineService;
    private final NetworkOrchestrationService networkOrchestrationService;
    private final EmergencyMonitoringService emergencyMonitoringService;
    
    public EmergencyConnectivityAgent(
            EmergencyContextService emergencyContextService,
            TrustValidationService trustValidationService,
            NetworkStateAssessmentService networkStateAssessmentService,
            EmergencyDecisionEngineService decisionEngineService,
            NetworkOrchestrationService networkOrchestrationService,
            EmergencyMonitoringService emergencyMonitoringService) {
        super("emergency-connectivity-agent", "Emergency Connectivity Agent",
                "Ensures guaranteed connectivity for emergency services and critical situations using Emergency Connectivity Setup");
        this.emergencyContextService = emergencyContextService;
        this.trustValidationService = trustValidationService;
        this.networkStateAssessmentService = networkStateAssessmentService;
        this.decisionEngineService = decisionEngineService;
        this.networkOrchestrationService = networkOrchestrationService;
        this.emergencyMonitoringService = emergencyMonitoringService;
        setPriority(10); // Highest priority
        setExecutionInterval(10); // Run every 10 seconds for real-time monitoring
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Emergency Connectivity Agent executing for device: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        try {
            // BR-1: Check for active emergency context
            GlobalResponse activeEmergenciesResponse = emergencyContextService
                    .getActiveEmergenciesByPhoneNumber(context.getPhoneNumber());
            
            boolean hasActiveEmergency = false;
            EmergencyContextDTO emergencyContext = null;
            
            if (activeEmergenciesResponse != null && activeEmergenciesResponse.getData() != null) {
                Object data = activeEmergenciesResponse.getData();
                if (data instanceof List && !((List<?>) data).isEmpty()) {
                    hasActiveEmergency = true;
                    // Get the first active emergency
                    Map<String, Object> emergencyMap = (Map<String, Object>) ((List<?>) data).get(0);
                    emergencyContext = mapToEmergencyContext(emergencyMap);
                }
            }
            
            // Check if emergency should be activated based on context
            boolean shouldActivate = shouldActivateEmergencyMode(context);
            
            if (hasActiveEmergency && emergencyContext != null) {
                // Process existing emergency
                return processActiveEmergency(emergencyContext, context, executedActions, recommendations);
            } else if (shouldActivate) {
                // BR-1: Detect and create emergency context
                return detectAndProcessEmergency(context, executedActions, recommendations);
            } else {
                // Monitoring mode - no emergency detected
                return AgentResult.builder()
                        .agentId(getId())
                        .success(true)
                        .confidence(0.85)
                        .message("Monitoring emergency connectivity - ready to activate if needed")
                        .actions(executedActions)
                        .recommendations(recommendations)
                        .metadata(Map.of("emergencyMode", false, "monitoring", true))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in Emergency Connectivity Agent execution", e);
            return AgentResult.builder()
                    .agentId(getId())
                    .success(false)
                    .confidence(0.0)
                    .error("Failed to execute emergency connectivity agent: " + e.getMessage())
                    .actions(executedActions)
                    .recommendations(recommendations)
                    .build();
        }
    }
    
    /**
     * Process an active emergency (BR-3 through BR-7)
     */
    private AgentResult processActiveEmergency(EmergencyContextDTO emergencyContext, 
                                               AgentContext context,
                                               List<AgentAction> executedActions,
                                               List<String> recommendations) {
        log.warn("Processing active emergency: {} for device: {}", 
                emergencyContext.getEmergencyId(), context.getPhoneNumber());
        
        try {
            // BR-3: Trust & Authorization Validation
            GlobalResponse trustResponse = trustValidationService.validateDeviceTrust(
                    emergencyContext.getPhoneNumber(),
                    emergencyContext.getDeviceImei(),
                    emergencyContext.getDeviceRole()
            );
            
            TrustValidationDTO trustValidation = null;
            if (trustResponse != null && trustResponse.getData() != null) {
                trustValidation = mapToTrustValidation((Map<String, Object>) trustResponse.getData());
            }
            
            if (trustValidation == null || 
                trustValidation.getStatus() != TrustValidationDTO.ValidationStatus.TRUSTED) {
                recommendations.add("WARNING: Device trust validation failed - emergency connectivity may be limited");
                return AgentResult.builder()
                        .agentId(getId())
                        .success(false)
                        .confidence(0.3)
                        .message("Emergency detected but device trust validation failed")
                        .actions(executedActions)
                        .recommendations(recommendations)
                        .build();
            }
            
            // BR-4: Network State Assessment
            GlobalResponse networkStateResponse = networkStateAssessmentService.assessNetworkState(
                    emergencyContext.getLatitude(),
                    emergencyContext.getLongitude()
            );
            
            NetworkStateDTO networkState = null;
            if (networkStateResponse != null && networkStateResponse.getData() != null) {
                networkState = mapToNetworkState((Map<String, Object>) networkStateResponse.getData());
            }
            
            // BR-5: AI Decision Engine
            GlobalResponse decisionResponse = decisionEngineService.evaluateEmergency(
                    emergencyContext,
                    trustValidation,
                    networkState
            );
            
            DecisionResultDTO decisionResult = null;
            if (decisionResponse != null && decisionResponse.getData() != null) {
                decisionResult = mapToDecisionResult((Map<String, Object>) decisionResponse.getData());
            }
            
            if (decisionResult != null && 
                decisionResult.getStatus() == DecisionResultDTO.DecisionStatus.APPROVED &&
                decisionResult.getConfidenceScore() >= 0.95) {
                
                // BR-6: Network Orchestration - Execute Guaranteed Connectivity
                GlobalResponse orchestrationResponse = networkOrchestrationService
                        .executeGuaranteedConnectivity(decisionResult);
                
                if (orchestrationResponse != null && orchestrationResponse.getCode() != null && 
                    orchestrationResponse.getCode() == 200) {
                    executedActions.add(AgentAction.builder()
                            .actionType("GUARANTEED_CONNECTIVITY")
                            .target("network-orchestration-service")
                            .reason("Emergency approved - guaranteed connectivity executed")
                            .status(AgentAction.ActionStatus.SUCCESS)
                            .parameters(Map.of("emergencyId", emergencyContext.getEmergencyId(),
                                    "confidence", decisionResult.getConfidenceScore()))
                            .build());
                    
                    recommendations.add("Guaranteed connectivity activated - < 1 second QoS activation");
                    recommendations.add("Emergency traffic is untouchable");
                    
                    // BR-7: Continuous Monitoring
                    // Extract QoS session ID from orchestration response if available
                    String qosSessionId = null;
                    if (orchestrationResponse.getData() != null) {
                        Map<String, Object> orchestrationData = (Map<String, Object>) orchestrationResponse.getData();
                        if (orchestrationData.containsKey("qosSessionId")) {
                            qosSessionId = (String) orchestrationData.get("qosSessionId");
                        }
                    }
                    
                    if (qosSessionId != null) {
                        GlobalResponse monitoringResponse = emergencyMonitoringService
                                .startMonitoring(emergencyContext.getEmergencyId(), qosSessionId);
                        
                        if (monitoringResponse != null && monitoringResponse.getCode() != null && 
                            monitoringResponse.getCode() == 200) {
                            recommendations.add("Continuous monitoring started - self-healing connectivity enabled");
                        }
                    }
                }
            }
            
            return AgentResult.builder()
                    .agentId(getId())
                    .success(true)
                    .confidence(decisionResult != null ? decisionResult.getConfidenceScore() : 0.9)
                    .message("Emergency connectivity mode active - maximum QoS guaranteed")
                    .actions(executedActions)
                    .recommendations(recommendations)
                    .metadata(Map.of("emergencyId", emergencyContext.getEmergencyId(),
                            "emergencyMode", true, "priority", 0))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing active emergency", e);
            return AgentResult.builder()
                    .agentId(getId())
                    .success(false)
                    .confidence(0.5)
                    .error("Failed to process emergency: " + e.getMessage())
                    .actions(executedActions)
                    .recommendations(recommendations)
                    .build();
        }
    }
    
    /**
     * Detect and process new emergency (BR-1, BR-2)
     */
    private AgentResult detectAndProcessEmergency(AgentContext context,
                                                   List<AgentAction> executedActions,
                                                   List<String> recommendations) {
        log.warn("Detecting new emergency for device: {}", context.getPhoneNumber());
        
        try {
            // Determine emergency type and create context
            Double latitude = context.getNetworkData().getLocation() != null ? 
                    context.getNetworkData().getLocation().getLatitude() : null;
            Double longitude = context.getNetworkData().getLocation() != null ? 
                    context.getNetworkData().getLocation().getLongitude() : null;
            
            // BR-1: Detect emergency (using SOS as default for agent-triggered)
            GlobalResponse emergencyResponse = emergencyContextService.detectEmergencyFromSOS(
                    context.getPhoneNumber(),
                    context.getNetworkData().getDeviceStatus() != null ? 
                            context.getNetworkData().getDeviceStatus().getImei() : "unknown",
                    latitude != null ? latitude : 0.0,
                    longitude != null ? longitude : 0.0
            );
            
            if (emergencyResponse != null && emergencyResponse.getCode() != null && 
                emergencyResponse.getCode() == 200) {
                // BR-2: Emergency event is automatically broadcast via Kafka
                recommendations.add("Emergency context detected and event broadcast");
                
                // Get the created emergency context
                if (emergencyResponse.getData() != null) {
                    Map<String, Object> emergencyData = (Map<String, Object>) emergencyResponse.getData();
                    String emergencyId = (String) emergencyData.get("emergencyId");
                    
                    if (emergencyId != null) {
                        GlobalResponse getContextResponse = emergencyContextService.getEmergencyContext(emergencyId);
                        if (getContextResponse != null && getContextResponse.getData() != null) {
                            EmergencyContextDTO emergencyContext = mapToEmergencyContext(
                                    (Map<String, Object>) getContextResponse.getData());
                            return processActiveEmergency(emergencyContext, context, executedActions, recommendations);
                        }
                    }
                }
            }
            
            return AgentResult.builder()
                    .agentId(getId())
                    .success(false)
                    .confidence(0.5)
                    .message("Emergency detected but context creation failed")
                    .actions(executedActions)
                    .recommendations(recommendations)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error detecting emergency", e);
            return AgentResult.builder()
                    .agentId(getId())
                    .success(false)
                    .confidence(0.3)
                    .error("Failed to detect emergency: " + e.getMessage())
                    .actions(executedActions)
                    .recommendations(recommendations)
                    .build();
        }
    }
    
    // Helper methods to map response data to DTOs
    private EmergencyContextDTO mapToEmergencyContext(Map<String, Object> map) {
        if (map == null) return null;
        return EmergencyContextDTO.builder()
                .emergencyId((String) map.get("emergencyId"))
                .phoneNumber((String) map.get("phoneNumber"))
                .deviceImei((String) map.get("deviceImei"))
                .emergencyType(map.get("emergencyType") != null ? 
                        EmergencyContextDTO.EmergencyType.valueOf(map.get("emergencyType").toString()) : null)
                .deviceRole(map.get("deviceRole") != null ? 
                        EmergencyContextDTO.DeviceRole.valueOf(map.get("deviceRole").toString()) : null)
                .severity(map.get("severity") != null ? 
                        EmergencyContextDTO.EmergencySeverity.valueOf(map.get("severity").toString()) : null)
                .latitude(map.get("latitude") != null ? ((Number) map.get("latitude")).doubleValue() : null)
                .longitude(map.get("longitude") != null ? ((Number) map.get("longitude")).doubleValue() : null)
                .status(map.get("status") != null ? 
                        EmergencyContextDTO.EmergencyStatus.valueOf(map.get("status").toString()) : null)
                .build();
    }
    
    private TrustValidationDTO mapToTrustValidation(Map<String, Object> map) {
        if (map == null) return null;
        return TrustValidationDTO.builder()
                .phoneNumber((String) map.get("phoneNumber"))
                .deviceImei((String) map.get("deviceImei"))
                .status(map.get("status") != null ? 
                        TrustValidationDTO.ValidationStatus.valueOf(map.get("status").toString()) : null)
                .trustScore(map.get("trustScore") != null ? ((Number) map.get("trustScore")).doubleValue() : null)
                .build();
    }
    
    private NetworkStateDTO mapToNetworkState(Map<String, Object> map) {
        if (map == null) return null;
        return NetworkStateDTO.builder()
                .congestionLevel(map.get("congestionLevel") != null ? 
                        NetworkStateDTO.CongestionLevel.valueOf(map.get("congestionLevel").toString()) : null)
                .qosCapacityStatus(map.get("qosCapacityStatus") != null ? 
                        NetworkStateDTO.QoSCapacityStatus.valueOf(map.get("qosCapacityStatus").toString()) : null)
                .build();
    }
    
    private DecisionResultDTO mapToDecisionResult(Map<String, Object> map) {
        if (map == null) return null;
        return DecisionResultDTO.builder()
                .decisionId((String) map.get("decisionId"))
                .status(map.get("status") != null ? 
                        DecisionResultDTO.DecisionStatus.valueOf(map.get("status").toString()) : null)
                .confidenceScore(map.get("confidenceScore") != null ? 
                        ((Number) map.get("confidenceScore")).doubleValue() : null)
                .build();
    }
    
    private boolean shouldActivateEmergencyMode(AgentContext context) {
        // Activate if connectivity is poor or device is critical
        boolean poorConnectivity = context.getNetworkData().getConnectivity() != null &&
                (Boolean.FALSE.equals(context.getNetworkData().getConnectivity().getIsConnected()) ||
                 (context.getNetworkData().getConnectivity().getSignalStrength() != null &&
                  context.getNetworkData().getConnectivity().getSignalStrength() < 30));
        
        boolean highLatency = context.getNetworkData().getConnectivity() != null &&
                context.getNetworkData().getConnectivity().getLatency() != null &&
                context.getNetworkData().getConnectivity().getLatency() > 100;
        
        return poorConnectivity || highLatency;
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
