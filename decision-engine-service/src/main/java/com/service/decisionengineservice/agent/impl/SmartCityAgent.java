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
 * Smart City Agent - Monitors city infrastructure (traffic lights, sensors, cameras)
 * Similar to CityCare infrastructure reporting
 */
@Slf4j
@Component
public class SmartCityAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
    @Value("${services.connectivity.base-url:http://localhost:8081}")
    private String connectivityServiceUrl;
    
    @Value("${services.location.base-url:http://localhost:8083}")
    private String locationServiceUrl;
    
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
            Map<String, Object> qosRequest = new HashMap<>();
            Map<String, Object> device = new HashMap<>();
            device.put("phoneNumber", context.getPhoneNumber());
            qosRequest.put("device", device);
            qosRequest.put("qosProfile", "HIGH_BANDWIDTH");
            qosRequest.put("duration", 3600);
            
            AgentAction action = AgentAction.builder()
                    .actionType("QOS_BOOST")
                    .target("connectivity-service")
                    .reason("Critical infrastructure requires guaranteed connectivity")
                    .status(AgentAction.ActionStatus.PENDING)
                    .parameters(Map.of("priority", 1, "bandwidth", 100.0))
                    .build();
            
            internalServiceClient.callService(connectivityServiceUrl, "/connectivity/Qos/sessions/create", qosRequest)
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
            Map<String, Object> locationRequest = new HashMap<>();
            Map<String, Object> device = new HashMap<>();
            device.put("phoneNumber", context.getPhoneNumber());
            locationRequest.put("device", device);
            
            internalServiceClient.callService(locationServiceUrl, "/location/verify/v1", locationRequest)
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
