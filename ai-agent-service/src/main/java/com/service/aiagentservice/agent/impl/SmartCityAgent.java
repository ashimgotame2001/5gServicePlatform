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
 * Smart City Agent - Monitors city infrastructure (traffic lights, sensors, cameras)
 * Similar to CityCare infrastructure reporting
 */
@Slf4j
@Component
public class SmartCityAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
    public SmartCityAgent(InternalServiceClient internalServiceClient) {
        super("smart-city-agent", "Smart City Infrastructure Agent",
                "Monitors and manages city infrastructure devices with guaranteed connectivity");
        this.internalServiceClient = internalServiceClient;
        setPriority(9); // Very high priority for critical infrastructure
        setExecutionInterval(20); // Run every 20 seconds
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Smart City Agent executing for infrastructure device: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        // Check device connectivity
        boolean isConnected = context.getNetworkData().getConnectivity() != null &&
                Boolean.TRUE.equals(context.getNetworkData().getConnectivity().getIsConnected());
        
        // Check signal strength
        Integer signalStrength = context.getNetworkData().getConnectivity() != null ?
                context.getNetworkData().getConnectivity().getSignalStrength() : null;
        
        metrics.put("isConnected", isConnected);
        metrics.put("signalStrength", signalStrength);
        
        // Critical infrastructure requires guaranteed connectivity
        if (!isConnected || (signalStrength != null && signalStrength < 50)) {
            log.warn("Critical infrastructure device has connectivity issues: {}", context.getPhoneNumber());
            
            // Request QoS boost for critical infrastructure
            Map<String, Object> qosRequest = Map.of(
                    "phoneNumber", context.getPhoneNumber(),
                    "priority", 1, // Highest priority
                    "bandwidth", 100.0,
                    "reason", "Critical infrastructure - Smart City"
            );
            
            AgentAction action = AgentAction.builder()
                    .actionType("QOS_BOOST")
                    .target("connectivity-service")
                    .reason("Critical infrastructure requires guaranteed connectivity")
                    .status(AgentAction.ActionStatus.PENDING)
                    .parameters(Map.of("priority", 1, "bandwidth", 100.0))
                    .build();
            
            internalServiceClient.requestQoSAdjustment(qosRequest)
                    .subscribe(
                            result -> {
                                action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                action.setResult(result);
                                log.info("QoS boost applied for infrastructure device");
                            },
                            error -> {
                                action.setStatus(AgentAction.ActionStatus.FAILED);
                                action.setError(error.getMessage());
                                log.error("Failed to boost QoS for infrastructure", error);
                            }
                    );
            
            executedActions.add(action);
            recommendations.add("QoS boosted for critical infrastructure device");
        }
        
        // Verify device location (for asset tracking)
        if (context.getNetworkData().getLocation() != null) {
            internalServiceClient.verifyLocation(context.getPhoneNumber())
                    .subscribe(
                            result -> log.debug("Location verified for infrastructure device"),
                            error -> log.warn("Location verification failed", error)
                    );
        }
        
        // Generate infrastructure health report
        String healthStatus = isConnected && signalStrength != null && signalStrength >= 50 ? "HEALTHY" : "NEEDS_ATTENTION";
        metrics.put("healthStatus", healthStatus);
        
        String message = healthStatus.equals("HEALTHY") 
                ? "Infrastructure device is healthy"
                : "Infrastructure device requires attention - actions taken";
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(0.9)
                .message(message)
                .actions(executedActions)
                .recommendations(recommendations)
                .metrics(metrics)
                .metadata(Map.of("infrastructureType", "SMART_CITY", "healthStatus", healthStatus))
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
