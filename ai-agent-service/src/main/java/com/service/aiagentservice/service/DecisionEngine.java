package com.service.aiagentservice.service;

import com.service.aiagentservice.agent.model.AgentAction;
import com.service.aiagentservice.agent.model.NetworkData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Decision engine for making autonomous decisions based on network data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionEngine {
    
    @Value("${ai.agents.decision-engine.confidence-threshold:0.7}")
    private double confidenceThreshold;
    
    /**
     * Analyze network data and determine if QoS adjustment is needed
     */
    public DecisionResult analyzeQoSRequirement(NetworkData networkData) {
        List<AgentAction> actions = new ArrayList<>();
        double confidence = 0.0;
        String reason = "";
        
        NetworkData.ConnectivityMetrics connectivity = networkData.getConnectivity();
        NetworkData.QoSMetrics qos = networkData.getQos();
        
        // Rule 1: Low signal strength requires QoS boost
        if (connectivity.getSignalStrength() != null && connectivity.getSignalStrength() < 50) {
            confidence += 0.3;
            reason += "Low signal strength detected. ";
        }
        
        // Rule 2: High latency requires QoS adjustment
        if (connectivity.getLatency() != null && connectivity.getLatency() > 100) {
            confidence += 0.3;
            reason += "High latency detected. ";
        }
        
        // Rule 3: Low throughput requires bandwidth increase
        if (connectivity.getThroughput() != null && connectivity.getThroughput() < 10.0) {
            confidence += 0.2;
            reason += "Low throughput detected. ";
        }
        
        // Rule 4: Current QoS profile is not optimal
        if (qos.getQosProfile() != null && qos.getQosProfile().equals("DEFAULT")) {
            confidence += 0.2;
            reason += "Default QoS profile in use. ";
        }
        
        if (confidence >= confidenceThreshold) {
            AgentAction action = AgentAction.builder()
                    .actionType("QOS_ADJUSTMENT")
                    .target("connectivity-service")
                    .reason(reason.trim())
                    .status(AgentAction.ActionStatus.PENDING)
                    .build();
            
            // Determine QoS parameters based on network conditions
            if (connectivity.getSignalStrength() != null && connectivity.getSignalStrength() < 50) {
                action.getParameters().put("priority", 1); // High priority
                action.getParameters().put("bandwidth", 100.0); // Increase bandwidth
            }
            if (connectivity.getLatency() != null && connectivity.getLatency() > 100) {
                action.getParameters().put("latency", 50); // Target latency
            }
            
            actions.add(action);
        }
        
        return DecisionResult.builder()
                .shouldAct(confidence >= confidenceThreshold)
                .confidence(confidence)
                .reason(reason.trim())
                .actions(actions)
                .build();
    }
    
    /**
     * Analyze location data and determine if verification is needed
     */
    public DecisionResult analyzeLocationVerification(NetworkData networkData) {
        List<AgentAction> actions = new ArrayList<>();
        double confidence = 0.0;
        String reason = "";
        
        NetworkData.LocationData location = networkData.getLocation();
        
        // Rule 1: Location data is stale (maxAge exceeded)
        if (location.getMaxAge() != null && location.getMaxAge() > 120) {
            confidence = 0.8;
            reason = "Location data is stale, verification needed. ";
            
            AgentAction action = AgentAction.builder()
                    .actionType("LOCATION_VERIFY")
                    .target("location-service")
                    .reason(reason)
                    .status(AgentAction.ActionStatus.PENDING)
                    .build();
            
            actions.add(action);
        }
        
        // Rule 2: Location accuracy is low
        if (location.getAccuracy() != null && location.getAccuracy() > 100) {
            confidence = Math.max(confidence, 0.7);
            reason += "Low location accuracy detected. ";
            
            if (actions.isEmpty()) {
                AgentAction action = AgentAction.builder()
                        .actionType("LOCATION_VERIFY")
                        .target("location-service")
                        .reason(reason)
                        .status(AgentAction.ActionStatus.PENDING)
                        .build();
                actions.add(action);
            }
        }
        
        return DecisionResult.builder()
                .shouldAct(confidence >= confidenceThreshold)
                .confidence(confidence)
                .reason(reason.trim())
                .actions(actions)
                .build();
    }
    
    /**
     * Analyze device status and determine if device swap is needed
     */
    public DecisionResult analyzeDeviceSwap(NetworkData networkData) {
        List<AgentAction> actions = new ArrayList<>();
        double confidence = 0.0;
        String reason = "";
        
        NetworkData.DeviceStatus deviceStatus = networkData.getDeviceStatus();
        
        // Rule 1: Device is inactive
        if (deviceStatus.getIsActive() != null && !deviceStatus.getIsActive()) {
            confidence = 0.9;
            reason = "Device is inactive, swap may be needed. ";
            
            AgentAction action = AgentAction.builder()
                    .actionType("DEVICE_SWAP")
                    .target("device-management-service")
                    .reason(reason)
                    .status(AgentAction.ActionStatus.PENDING)
                    .build();
            
            actions.add(action);
        }
        
        // Rule 2: Device status is unknown or error
        if (deviceStatus.getStatus() != null && 
            (deviceStatus.getStatus().equals("ERROR") || deviceStatus.getStatus().equals("UNKNOWN"))) {
            confidence = Math.max(confidence, 0.8);
            reason += "Device status indicates problems. ";
            
            if (actions.isEmpty()) {
                AgentAction action = AgentAction.builder()
                        .actionType("DEVICE_SWAP")
                        .target("device-management-service")
                        .reason(reason)
                        .status(AgentAction.ActionStatus.PENDING)
                        .build();
                actions.add(action);
            }
        }
        
        return DecisionResult.builder()
                .shouldAct(confidence >= confidenceThreshold)
                .confidence(confidence)
                .reason(reason.trim())
                .actions(actions)
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class DecisionResult {
        private boolean shouldAct;
        private double confidence;
        private String reason;
        private List<AgentAction> actions;
    }
}
