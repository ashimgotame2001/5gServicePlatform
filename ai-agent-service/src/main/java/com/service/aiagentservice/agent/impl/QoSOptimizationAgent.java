package com.service.aiagentservice.agent.impl;

import com.service.aiagentservice.agent.BaseAgent;
import com.service.aiagentservice.agent.model.AgentAction;
import com.service.aiagentservice.agent.model.AgentContext;
import com.service.aiagentservice.agent.model.AgentResult;
import com.service.aiagentservice.service.DecisionEngine;
import com.service.aiagentservice.service.InternalServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent that autonomously optimizes QoS based on real network conditions
 */
@Slf4j
@Component
public class QoSOptimizationAgent extends BaseAgent {
    
    private final DecisionEngine decisionEngine;
    private final InternalServiceClient internalServiceClient;
    
    public QoSOptimizationAgent(DecisionEngine decisionEngine, InternalServiceClient internalServiceClient) {
        super("qos-optimization-agent", "QoS Optimization Agent", 
                "Autonomously optimizes Quality of Service based on real-time network conditions");
        this.decisionEngine = decisionEngine;
        this.internalServiceClient = internalServiceClient;
        setPriority(8); // High priority
        setExecutionInterval(30); // Run every 30 seconds
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.info("QoS Optimization Agent executing for phone: {}", context.getPhoneNumber());
        
        List<AgentAction> executedActions = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Analyze network data and make decision
        DecisionEngine.DecisionResult decision = decisionEngine.analyzeQoSRequirement(context.getNetworkData());
        
        if (decision.isShouldAct()) {
            log.info("QoS adjustment needed. Confidence: {}, Reason: {}", 
                    decision.getConfidence(), decision.getReason());
            
            // Execute actions
            for (AgentAction action : decision.getActions()) {
                try {
                    // Prepare QoS request
                    Map<String, Object> qosRequest = Map.of(
                            "phoneNumber", context.getPhoneNumber(),
                            "priority", action.getParameters().getOrDefault("priority", 1),
                            "bandwidth", action.getParameters().getOrDefault("bandwidth", 50.0),
                            "latency", action.getParameters().getOrDefault("latency", 50)
                    );
                    
                    // Execute the action
                    internalServiceClient.requestQoSAdjustment(qosRequest)
                            .subscribe(
                                    result -> {
                                        action.setStatus(AgentAction.ActionStatus.SUCCESS);
                                        action.setResult(result);
                                        log.info("QoS adjustment executed successfully");
                                    },
                                    error -> {
                                        action.setStatus(AgentAction.ActionStatus.FAILED);
                                        action.setError(error.getMessage());
                                        log.error("QoS adjustment failed", error);
                                    }
                            );
                    
                    executedActions.add(action);
                    recommendations.add("QoS adjusted to improve network performance");
                    
                } catch (Exception e) {
                    log.error("Error executing QoS action", e);
                    action.setStatus(AgentAction.ActionStatus.FAILED);
                    action.setError(e.getMessage());
                    executedActions.add(action);
                }
            }
        } else {
            log.debug("No QoS adjustment needed at this time");
            recommendations.add("Network conditions are optimal, no QoS adjustment needed");
        }
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(decision.getConfidence())
                .message(decision.getReason().isEmpty() ? "QoS analysis completed" : decision.getReason())
                .actions(executedActions)
                .recommendations(recommendations)
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        // Only execute if we have network data
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
