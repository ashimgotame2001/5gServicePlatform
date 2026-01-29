package com.service.decisionengineservice.agent.impl;

import com.service.decisionengineservice.agent.BaseAgent;
import com.service.decisionengineservice.agent.model.AgentAction;
import com.service.decisionengineservice.agent.model.AgentContext;
import com.service.decisionengineservice.agent.model.AgentResult;
import com.service.shared.service.InternalServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transportation Agent - Manages connectivity for transportation and event logistics
 */
@Slf4j
@Component
public class TransportationAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
    @Value("${services.connectivity.base-url:http://localhost:8081}")
    private String connectivityServiceUrl;
    
    @Value("${services.location.base-url:http://localhost:8083}")
    private String locationServiceUrl;
    
    @Value("${services.identification.base-url:http://localhost:8082}")
    private String identificationServiceUrl;
    
    public TransportationAgent(InternalServiceClient internalServiceClient) {
        super("transportation-agent", "Transportation & Logistics Agent",
                "Manages connectivity for transportation systems, fleet management, and event logistics");
        this.internalServiceClient = internalServiceClient;
        setPriority(7);
        setExecutionInterval(30); // Run every 30 seconds
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Transportation Agent executing for vehicle/asset: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Monitor location for geofencing
        if (context.getNetworkData().getLocation() != null) {
            // Verify location for tracking
            Map<String, Object> locationRequest = new HashMap<>();
            Map<String, Object> device = new HashMap<>();
            device.put("phoneNumber", context.getPhoneNumber());
            locationRequest.put("device", device);
            
            internalServiceClient.callService(locationServiceUrl, "/location/verify/v1", locationRequest)
                    .subscribe(
                            result -> {
                                log.debug("Vehicle/asset location verified");
                                recommendations.add("Location verified for tracking");
                            },
                            error -> log.warn("Location verification failed", error)
                    );
        }
        
        // Check connectivity for moving vehicles
        boolean isConnected = context.getNetworkData().getConnectivity() != null &&
                Boolean.TRUE.equals(context.getNetworkData().getConnectivity().getIsConnected());
        
        if (!isConnected) {
            log.warn("Transportation device disconnected: {}", context.getPhoneNumber());
            
            // Request QoS for reconnection
            Map<String, Object> qosRequest = new HashMap<>();
            Map<String, Object> device = new HashMap<>();
            device.put("phoneNumber", context.getPhoneNumber());
            qosRequest.put("device", device);
            qosRequest.put("qosProfile", "HIGH_BANDWIDTH");
            qosRequest.put("duration", 3600);
            
            AgentAction action = AgentAction.builder()
                    .actionType("TRANSPORTATION_QOS")
                    .target("connectivity-service")
                    .reason("Transportation tracking requires reliable connectivity")
                    .status(AgentAction.ActionStatus.PENDING)
                    .build();
            
            internalServiceClient.callService(connectivityServiceUrl, "/connectivity/Qos/sessions/create", qosRequest)
                    .subscribe(
                            result -> {
                                action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                action.setResult(result);
                                log.info("Transportation QoS applied");
                            },
                            error -> {
                                action.setStatus(AgentAction.ActionStatus.FAILED);
                                action.setError(error.getMessage());
                            }
                    );
            
            executedActions.add(action);
            recommendations.add("QoS applied for transportation tracking");
        }
        
        // Monitor device status for fleet management
        internalServiceClient.getFromService(identificationServiceUrl, 
                "/identification/share-phone-number?phoneNumber=" + context.getPhoneNumber())
                .subscribe(
                        status -> {
                            boolean isActive = Boolean.TRUE.equals(status.getOrDefault("isActive", false));
                            if (!isActive) {
                                recommendations.add("Vehicle/asset device may need maintenance");
                            }
                        },
                        error -> log.warn("Device status check failed", error)
                );
        
        String message = isConnected 
                ? "Transportation device connectivity is stable"
                : "Transportation device connectivity restored";
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(0.85)
                .message(message)
                .actions(executedActions)
                .recommendations(recommendations)
                .metadata(Map.of("useCase", "TRANSPORTATION", "trackingEnabled", true))
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
