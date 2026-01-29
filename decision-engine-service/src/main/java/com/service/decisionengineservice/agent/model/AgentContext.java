package com.service.decisionengineservice.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed to agents containing network data, state, and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {
    
    /**
     * Timestamp when this context was created
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Network data collected from various sources
     */
    @Builder.Default
    private NetworkData networkData = new NetworkData();
    
    /**
     * User/device information if applicable
     */
    private String userId;
    private String deviceId;
    private String phoneNumber;
    
    /**
     * Agent-specific state and metadata
     */
    @Builder.Default
    private Map<String, Object> state = new HashMap<>();
    
    /**
     * Historical data for trend analysis
     */
    @Builder.Default
    private Map<String, Object> historicalData = new HashMap<>();
    
    /**
     * Configuration parameters for this execution
     */
    @Builder.Default
    private Map<String, Object> config = new HashMap<>();
    
    /**
     * Previous agent results for coordination
     */
    @Builder.Default
    private Map<String, AgentResult> previousResults = new HashMap<>();
}
