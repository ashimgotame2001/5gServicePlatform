package com.service.decisionengineservice.agent.impl;

import com.service.decisionengineservice.agent.BaseAgent;
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
 * Public Safety Agent - Monitors public safety systems and sustainability
 */
@Slf4j
@Component
public class PublicSafetyAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
    @Value("${services.connectivity.base-url:http://localhost:8081}")
    private String connectivityServiceUrl;
    
    @Value("${services.identification.base-url:http://localhost:8082}")
    private String identificationServiceUrl;
    
    public PublicSafetyAgent(InternalServiceClient internalServiceClient) {
        super("public-safety-agent", "Public Safety & Sustainability Agent",
                "Monitors public safety devices, environmental sensors, and optimizes network resources for sustainability");
        this.internalServiceClient = internalServiceClient;
        setPriority(8); // High priority for public safety
        setExecutionInterval(20); // Run every 20 seconds
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Public Safety Agent executing for public safety device: {}", context.getPhoneNumber());
        
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        // Monitor connectivity for public safety devices
        boolean isConnected = context.getNetworkData().getConnectivity() != null &&
                Boolean.TRUE.equals(context.getNetworkData().getConnectivity().getIsConnected());
        
        Integer signalStrength = context.getNetworkData().getConnectivity() != null ?
                context.getNetworkData().getConnectivity().getSignalStrength() : null;
        
        metrics.put("isConnected", isConnected);
        metrics.put("signalStrength", signalStrength);
        
        // Public safety devices need reliable connectivity
        if (!isConnected || (signalStrength != null && signalStrength < 50)) {
            log.warn("Public safety device connectivity issue: {}", context.getPhoneNumber());
            
            // Request QoS for public safety
            Map<String, Object> qosRequest = new HashMap<>();
            Map<String, Object> device = new HashMap<>();
            device.put("phoneNumber", context.getPhoneNumber());
            qosRequest.put("device", device);
            qosRequest.put("qosProfile", "HIGH_BANDWIDTH");
            qosRequest.put("duration", 3600);
            
            internalServiceClient.callService(connectivityServiceUrl, "/connectivity/Qos/sessions/create", qosRequest)
                    .subscribe(
                            result -> {
                                log.info("Public safety QoS applied");
                                recommendations.add("QoS optimized for public safety device");
                            },
                            error -> log.error("Failed to apply public safety QoS", error)
                    );
        }
        
        // Monitor location for population density analysis
        if (context.getNetworkData().getLocation() != null) {
            // Location data can be used for population density analysis
            recommendations.add("Location data available for population density analysis");
        }
        
        // Verify device status
        internalServiceClient.getFromService(identificationServiceUrl, 
                "/identification/share-phone-number?phoneNumber=" + context.getPhoneNumber())
                .subscribe(
                        status -> {
                            boolean isActive = Boolean.TRUE.equals(status.getOrDefault("isActive", false));
                            if (!isActive) {
                                recommendations.add("Public safety device requires attention");
                            }
                        },
                        error -> log.warn("Device status check failed", error)
                );
        
        String healthStatus = isConnected && signalStrength != null && signalStrength >= 50 ? "HEALTHY" : "NEEDS_ATTENTION";
        metrics.put("healthStatus", healthStatus);
        
        String message = healthStatus.equals("HEALTHY")
                ? "Public safety device is operating normally"
                : "Public safety device connectivity optimized";
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(0.9)
                .message(message)
                .recommendations(recommendations)
                .metrics(metrics)
                .metadata(Map.of("useCase", "PUBLIC_SAFETY", "sustainabilityOptimized", true))
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
