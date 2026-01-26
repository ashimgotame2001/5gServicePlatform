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
 * Emergency Connectivity Agent - Guarantees connectivity for emergency services
 */
@Slf4j
@Component
public class EmergencyConnectivityAgent extends BaseAgent {
    
    private final InternalServiceClient internalServiceClient;
    
    public EmergencyConnectivityAgent(InternalServiceClient internalServiceClient) {
        super("emergency-connectivity-agent", "Emergency Connectivity Agent",
                "Ensures guaranteed connectivity for emergency services and critical situations");
        this.internalServiceClient = internalServiceClient;
        setPriority(10); // Highest priority
        setExecutionInterval(10); // Run every 10 seconds for real-time monitoring
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Emergency Connectivity Agent executing for emergency device: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Check if this is an emergency situation (can be determined from context state)
        boolean isEmergency = context.getState().getOrDefault("emergency", false).equals(true) ||
                context.getState().getOrDefault("critical", false).equals(true);
        
        if (isEmergency || shouldActivateEmergencyMode(context)) {
            log.warn("Emergency mode activated for device: {}", context.getPhoneNumber());
            
            // Request highest QoS priority
            Map<String, Object> qosRequest = Map.of(
                    "phoneNumber", context.getPhoneNumber(),
                    "priority", 0, // Highest possible priority
                    "bandwidth", 200.0, // Maximum bandwidth
                    "latency", 10, // Minimum latency
                    "reason", "EMERGENCY_CONNECTIVITY"
            );
            
            AgentAction qosAction = AgentAction.builder()
                    .actionType("EMERGENCY_QOS")
                    .target("connectivity-service")
                    .reason("Emergency situation - maximum QoS required")
                    .status(AgentAction.ActionStatus.PENDING)
                    .parameters(Map.of("priority", 0, "bandwidth", 200.0, "latency", 10))
                    .build();
            
            internalServiceClient.requestQoSAdjustment(qosRequest)
                    .subscribe(
                            result -> {
                                qosAction.setStatus(AgentAction.ActionStatus.SUCCESS);
                                qosAction.setResult(result);
                                log.info("Emergency QoS activated");
                            },
                            error -> {
                                qosAction.setStatus(AgentAction.ActionStatus.FAILED);
                                qosAction.setError(error.getMessage());
                                log.error("Failed to activate emergency QoS", error);
                            }
                    );
            
            executedActions.add(qosAction);
            recommendations.add("Emergency QoS activated - maximum priority and bandwidth allocated");
            
            // Verify device reachability
            internalServiceClient.getConnectivityStatus(context.getPhoneNumber())
                    .subscribe(
                            status -> {
                                log.info("Device reachability verified: {}", status);
                                recommendations.add("Device reachability confirmed");
                            },
                            error -> {
                                log.error("Device reachability check failed", error);
                                recommendations.add("WARNING: Device reachability check failed");
                            }
                    );
        }
        
        String message = isEmergency 
                ? "Emergency connectivity mode active - maximum QoS guaranteed"
                : "Monitoring emergency connectivity - ready to activate if needed";
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(0.95)
                .message(message)
                .actions(executedActions)
                .recommendations(recommendations)
                .metadata(Map.of("emergencyMode", isEmergency, "priority", 0))
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
