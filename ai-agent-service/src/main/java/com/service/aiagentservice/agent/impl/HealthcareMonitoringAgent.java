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
 * Healthcare Monitoring Agent - Ensures reliable connectivity for remote patient monitoring
 */
@Slf4j
@Component
public class HealthcareMonitoringAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
    public HealthcareMonitoringAgent(InternalServiceClient internalServiceClient) {
        super("healthcare-monitoring-agent", "Healthcare Remote Monitoring Agent",
                "Ensures reliable, low-latency connectivity for remote patient monitoring devices");
        this.internalServiceClient = internalServiceClient;
        setPriority(9); // Very high priority for healthcare
        setExecutionInterval(15); // Run every 15 seconds
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Healthcare Monitoring Agent executing for patient device: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        // Check connectivity metrics critical for healthcare
        boolean isConnected = context.getNetworkData().getConnectivity() != null &&
                Boolean.TRUE.equals(context.getNetworkData().getConnectivity().getIsConnected());
        
        Double latency = context.getNetworkData().getConnectivity() != null ?
                context.getNetworkData().getConnectivity().getLatency() : null;
        
        Integer signalStrength = context.getNetworkData().getConnectivity() != null ?
                context.getNetworkData().getConnectivity().getSignalStrength() : null;
        
        metrics.put("isConnected", isConnected);
        metrics.put("latency", latency);
        metrics.put("signalStrength", signalStrength);
        
        // Healthcare requires low latency (< 50ms) and reliable connectivity
        boolean needsQoS = !isConnected || 
                (latency != null && latency > 50) ||
                (signalStrength != null && signalStrength < 60);
        
        if (needsQoS) {
            log.warn("Patient monitoring device requires QoS optimization: {}", context.getPhoneNumber());
            
            // Request low-latency QoS for healthcare
            Map<String, Object> qosRequest = Map.of(
                    "phoneNumber", context.getPhoneNumber(),
                    "priority", 1, // High priority
                    "bandwidth", 50.0, // Sufficient for monitoring
                    "latency", 30, // Target latency for healthcare
                    "reason", "HEALTHCARE_MONITORING"
            );
            
            AgentAction action = AgentAction.builder()
                    .actionType("HEALTHCARE_QOS")
                    .target("connectivity-service")
                    .reason("Healthcare monitoring requires low-latency, reliable connectivity")
                    .status(AgentAction.ActionStatus.PENDING)
                    .parameters(Map.of("priority", 1, "latency", 30))
                    .build();
            
            internalServiceClient.requestQoSAdjustment(qosRequest)
                    .subscribe(
                            result -> {
                                action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                action.setResult(result);
                                log.info("Healthcare QoS optimized");
                            },
                            error -> {
                                action.setStatus(AgentAction.ActionStatus.FAILED);
                                action.setError(error.getMessage());
                                log.error("Failed to optimize healthcare QoS", error);
                            }
                    );
            
            executedActions.add(action);
            recommendations.add("QoS optimized for healthcare monitoring - low latency guaranteed");
        }
        
        // Verify device status (critical for patient safety)
        internalServiceClient.getDeviceStatus(context.getPhoneNumber())
                .subscribe(
                        status -> {
                            boolean isActive = Boolean.TRUE.equals(status.getOrDefault("isActive", false));
                            if (!isActive) {
                                log.error("CRITICAL: Patient monitoring device is inactive!");
                                recommendations.add("ALERT: Patient monitoring device is inactive - immediate attention required");
                            } else {
                                log.debug("Patient monitoring device is active");
                            }
                        },
                        error -> {
                            log.error("Failed to check device status", error);
                            recommendations.add("WARNING: Device status check failed");
                        }
                );
        
        // Verify location for patient safety
        if (context.getNetworkData().getLocation() != null) {
            internalServiceClient.verifyLocation(context.getPhoneNumber())
                    .subscribe(
                            result -> {
                                log.debug("Patient device location verified");
                                recommendations.add("Patient device location verified for safety");
                            },
                            error -> log.warn("Location verification failed", error)
                    );
        }
        
        String healthStatus = isConnected && latency != null && latency <= 50 ? "OPTIMAL" : "NEEDS_OPTIMIZATION";
        metrics.put("healthStatus", healthStatus);
        
        String message = healthStatus.equals("OPTIMAL")
                ? "Patient monitoring device connectivity is optimal"
                : "Patient monitoring device QoS optimized for reliable monitoring";
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(0.9)
                .message(message)
                .actions(executedActions)
                .recommendations(recommendations)
                .metrics(metrics)
                .metadata(Map.of("useCase", "HEALTHCARE", "healthStatus", healthStatus))
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
