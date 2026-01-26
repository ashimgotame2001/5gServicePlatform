package com.service.aiagentservice.agent.impl;

import com.service.aiagentservice.agent.BaseAgent;
import com.service.aiagentservice.agent.model.AgentAction;
import com.service.aiagentservice.agent.model.AgentContext;
import com.service.aiagentservice.agent.model.AgentResult;
import com.service.aiagentservice.service.InternalServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transportation Agent - Manages connectivity for transportation and event logistics
 */
@Slf4j
@Component
public class TransportationAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
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
            internalServiceClient.verifyLocation(context.getPhoneNumber())
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
            Map<String, Object> qosRequest = Map.of(
                    "phoneNumber", context.getPhoneNumber(),
                    "priority", 2,
                    "bandwidth", 25.0,
                    "reason", "TRANSPORTATION_TRACKING"
            );
            
            AgentAction action = AgentAction.builder()
                    .actionType("TRANSPORTATION_QOS")
                    .target("connectivity-service")
                    .reason("Transportation tracking requires reliable connectivity")
                    .status(AgentAction.ActionStatus.PENDING)
                    .build();
            
            internalServiceClient.requestQoSAdjustment(qosRequest)
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
        internalServiceClient.getDeviceStatus(context.getPhoneNumber())
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
