package com.service.aiagentservice.agent.impl;

import com.service.aiagentservice.agent.BaseAgent;
import com.service.aiagentservice.agent.model.AgentAction;
import com.service.aiagentservice.agent.model.AgentContext;
import com.service.aiagentservice.agent.model.AgentResult;
import com.service.aiagentservice.service.DecisionEngine;
import com.service.shared.service.InternalServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent that autonomously verifies and manages location data
 */
@Slf4j
@Component
public class LocationVerificationAgent extends BaseAgent {
    
    private final DecisionEngine decisionEngine;
    private final InternalServiceClient internalServiceClient;
    
    @Value("${services.location.base-url:http://localhost:8083}")
    private String locationServiceUrl;
    
    public LocationVerificationAgent(DecisionEngine decisionEngine, InternalServiceClient internalServiceClient) {
        super("location-verification-agent", "Location Verification Agent",
                "Autonomously verifies and manages location data for devices");
        this.decisionEngine = decisionEngine;
        this.internalServiceClient = internalServiceClient;
        setPriority(6);
        setExecutionInterval(60); // Run every 60 seconds
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Location Verification Agent executing for phone: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Analyze location data and make decision
        DecisionEngine.DecisionResult decision = decisionEngine.analyzeLocationVerification(context.getNetworkData());
        
        if (decision.isShouldAct()) {
            log.info("Location verification needed. Confidence: {}, Reason: {}", 
                    decision.getConfidence(), decision.getReason());
            
            // Execute actions
            for (AgentAction action : decision.getActions()) {
                try {
                    // Execute location verification
                    Map<String, Object> locationRequest = new HashMap<>();
                    Map<String, Object> device = new HashMap<>();
                    device.put("phoneNumber", context.getPhoneNumber());
                    locationRequest.put("device", device);
                    
                    internalServiceClient.callService(locationServiceUrl, "/location/verify/v1", locationRequest)
                            .subscribe(
                                    result -> {
                                        action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                        action.setResult(result);
                                        log.info("Location verification executed successfully");
                                    },
                                    error -> {
                                        action.setStatus(AgentAction.ActionStatus.FAILED);
                                        action.setError(error.getMessage());
                                        log.error("Location verification failed", error);
                                    }
                            );
                    
                    executedActions.add(action);
                    recommendations.add("Location verified to ensure accurate positioning");
                    
                } catch (Exception e) {
                    log.error("Error executing location verification", e);
                    action.setStatus(AgentAction.ActionStatus.FAILED);
                    action.setError(e.getMessage());
                    executedActions.add(action);
                }
            }
        } else {
            log.debug("No location verification needed at this time");
            recommendations.add("Location data is current and accurate");
        }
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(decision.getConfidence())
                .message(decision.getReason().isEmpty() ? "Location analysis completed" : decision.getReason())
                .actions(executedActions)
                .recommendations(recommendations)
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
