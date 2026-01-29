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
 * Agent that autonomously manages device and SIM card operations
 */
@Slf4j
@Component
public class DeviceManagementAgent extends BaseAgent {
    
    private final DecisionEngine decisionEngine;
    private final InternalServiceClient internalServiceClient;
    
    @Value("${services.device-management.base-url:http://localhost:8084}")
    private String deviceManagementServiceUrl;
    
    public DeviceManagementAgent(DecisionEngine decisionEngine, InternalServiceClient internalServiceClient) {
        super("device-management-agent", "Device Management Agent",
                "Autonomously manages device and SIM card operations based on network conditions");
        this.decisionEngine = decisionEngine;
        this.internalServiceClient = internalServiceClient;
        setPriority(5);
        setExecutionInterval(120); // Run every 2 minutes
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("Device Management Agent executing for phone: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Analyze device status and make decision
        DecisionEngine.DecisionResult decision = decisionEngine.analyzeDeviceSwap(context.getNetworkData());
        
        if (decision.isShouldAct()) {
            log.info("Device management action needed. Confidence: {}, Reason: {}", 
                    decision.getConfidence(), decision.getReason());
            
            // Execute actions
            for (AgentAction action : decision.getActions()) {
                try {
                    if ("DEVICE_SWAP".equals(action.getActionType())) {
                        // Prepare device swap request
                        Map<String, Object> swapRequest = new HashMap<>();
                        Map<String, Object> device = new HashMap<>();
                        device.put("phoneNumber", context.getPhoneNumber());
                        swapRequest.put("device", device);
                        swapRequest.put("maxAge", 60);
                        
                        // Execute the action
                        internalServiceClient.callService(deviceManagementServiceUrl, "/device/swap/check", swapRequest)
                                .subscribe(
                                        result -> {
                                            action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                            action.setResult(result);
                                            log.info("Device swap check executed successfully");
                                        },
                                        error -> {
                                            action.setStatus(AgentAction.ActionStatus.FAILED);
                                            action.setError(error.getMessage());
                                            log.error("Device swap check failed", error);
                                        }
                                );
                    } else if ("SIM_SWAP".equals(action.getActionType())) {
                        // Prepare SIM swap request
                        Map<String, Object> swapRequest = new HashMap<>();
                        Map<String, Object> device = new HashMap<>();
                        device.put("phoneNumber", context.getPhoneNumber());
                        swapRequest.put("device", device);
                        swapRequest.put("maxAge", 60);
                        
                        // Execute the action
                        internalServiceClient.callService(deviceManagementServiceUrl, "/device/swap/check", swapRequest)
                                .subscribe(
                                        result -> {
                                            action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                            action.setResult(result);
                                            log.info("SIM swap check executed successfully");
                                        },
                                        error -> {
                                            action.setStatus(AgentAction.ActionStatus.FAILED);
                                            action.setError(error.getMessage());
                                            log.error("SIM swap check failed", error);
                                        }
                                );
                    }
                    
                    executedActions.add(action);
                    recommendations.add("Device management action executed to improve service");
                    
                } catch (Exception e) {
                    log.error("Error executing device management action", e);
                    action.setStatus(AgentAction.ActionStatus.FAILED);
                    action.setError(e.getMessage());
                    executedActions.add(action);
                }
            }
        } else {
            log.debug("No device management action needed at this time");
            recommendations.add("Device status is normal, no action required");
        }
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(decision.getConfidence())
                .message(decision.getReason().isEmpty() ? "Device management analysis completed" : decision.getReason())
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
