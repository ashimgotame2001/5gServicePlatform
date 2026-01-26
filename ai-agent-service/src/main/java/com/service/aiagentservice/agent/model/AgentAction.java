package com.service.aiagentservice.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an action taken by an agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentAction {
    
    /**
     * Type of action (e.g., "QOS_ADJUSTMENT", "DEVICE_SWAP", "LOCATION_VERIFY")
     */
    private String actionType;
    
    /**
     * Target service or resource
     */
    private String target;
    
    /**
     * Action parameters
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * Status of the action
     */
    @Builder.Default
    private ActionStatus status = ActionStatus.PENDING;
    
    /**
     * Timestamp when action was created
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Result of the action
     */
    private Object result;
    
    /**
     * Error message if action failed
     */
    private String error;
    
    /**
     * Reason for taking this action
     */
    private String reason;
    
    public enum ActionStatus {
        PENDING,
        EXECUTING,
        SUCCESS,
        FAILED,
        CANCELLED
    }
}
