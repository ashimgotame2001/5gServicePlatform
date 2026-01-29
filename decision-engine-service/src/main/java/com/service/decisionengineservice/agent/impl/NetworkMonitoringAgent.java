package com.service.decisionengineservice.agent.impl;

import com.service.decisionengineservice.agent.BaseAgent;
import com.service.decisionengineservice.agent.model.AgentContext;
import com.service.decisionengineservice.agent.model.AgentResult;
import com.service.decisionengineservice.agent.model.NetworkData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent that continuously monitors network conditions and detects anomalies
 */
@Slf4j
@Component
public class NetworkMonitoringAgent extends BaseAgent {
    
    public NetworkMonitoringAgent() {
        super("network-monitoring-agent", "Network Monitoring Agent",
                "Continuously monitors network conditions and detects anomalies in real-time");
        setPriority(7);
        setExecutionInterval(10); // Run every 10 seconds for real-time monitoring
    }
    
    @Override
    protected AgentResult doExecute(AgentContext context) {
        log.debug("Network Monitoring Agent executing for phone: {}", context.getPhoneNumber());
        
        NetworkData networkData = context.getNetworkData();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();
        List<String> anomalies = new ArrayList<>();
        
        // Monitor connectivity metrics
        if (networkData.getConnectivity() != null) {
            NetworkData.ConnectivityMetrics connectivity = networkData.getConnectivity();
            
            metrics.put("signalStrength", connectivity.getSignalStrength());
            metrics.put("latency", connectivity.getLatency());
            metrics.put("throughput", connectivity.getThroughput());
            metrics.put("networkType", connectivity.getNetworkType());
            
            // Detect anomalies
            if (connectivity.getSignalStrength() != null && connectivity.getSignalStrength() < 30) {
                anomalies.add("Critical: Very low signal strength detected");
                recommendations.add("Consider moving to area with better coverage");
            }
            
            if (connectivity.getLatency() != null && connectivity.getLatency() > 200) {
                anomalies.add("Warning: High latency detected");
                recommendations.add("Network congestion may be affecting performance");
            }
            
            if (connectivity.getThroughput() != null && connectivity.getThroughput() < 5.0) {
                anomalies.add("Warning: Low throughput detected");
                recommendations.add("Bandwidth may be insufficient for current usage");
            }
            
            if (connectivity.getIsConnected() != null && !connectivity.getIsConnected()) {
                anomalies.add("Critical: Device is disconnected from network");
                recommendations.add("Immediate attention required - device disconnected");
            }
        }
        
        // Monitor location data
        if (networkData.getLocation() != null) {
            NetworkData.LocationData location = networkData.getLocation();
            metrics.put("locationAccuracy", location.getAccuracy());
            metrics.put("locationAge", location.getMaxAge());
            
            if (location.getAccuracy() != null && location.getAccuracy() > 200) {
                anomalies.add("Warning: Low location accuracy");
                recommendations.add("Location data may not be reliable");
            }
        }
        
        // Monitor device status
        if (networkData.getDeviceStatus() != null) {
            NetworkData.DeviceStatus deviceStatus = networkData.getDeviceStatus();
            metrics.put("deviceStatus", deviceStatus.getStatus());
            metrics.put("deviceActive", deviceStatus.getIsActive());
            
            if (deviceStatus.getIsActive() != null && !deviceStatus.getIsActive()) {
                anomalies.add("Critical: Device is inactive");
                recommendations.add("Device may need attention or replacement");
            }
        }
        
        String message = anomalies.isEmpty() 
                ? "Network monitoring completed - no anomalies detected"
                : String.format("Network monitoring completed - %d anomaly(ies) detected", anomalies.size());
        
        return AgentResult.builder()
                .agentId(getId())
                .success(true)
                .confidence(anomalies.isEmpty() ? 1.0 : 0.8)
                .message(message)
                .recommendations(recommendations)
                .metrics(metrics)
                .metadata(Map.of("anomalies", anomalies, "anomalyCount", anomalies.size()))
                .build();
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        return super.shouldExecute(context) && 
               context.getNetworkData() != null &&
               context.getPhoneNumber() != null;
    }
}
