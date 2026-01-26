package com.service.aiagentservice.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of an agent's execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {
    
    /**
     * ID of the agent that produced this result
     */
    private String agentId;
    
    /**
     * Timestamp when result was generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Success status
     */
    @Builder.Default
    private boolean success = false;
    
    /**
     * Confidence level (0.0 to 1.0)
     */
    @Builder.Default
    private double confidence = 0.0;
    
    /**
     * Result message/description
     */
    private String message;
    
    /**
     * Actions taken by the agent
     */
    @Builder.Default
    private List<AgentAction> actions = new ArrayList<>();
    
    /**
     * Recommendations or insights
     */
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
    
    /**
     * Metrics and measurements
     */
    @Builder.Default
    private Map<String, Object> metrics = new HashMap<>();
    
    /**
     * Additional metadata
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Error information if execution failed
     */
    private String error;
    
    /**
     * Execution time in milliseconds
     */
    private long executionTimeMs;
}
