package com.service.aiagentservice.agent;

import com.service.aiagentservice.agent.model.AgentContext;
import com.service.aiagentservice.agent.model.AgentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of Agent interface providing common functionality
 */
@Slf4j
@Getter
@Setter
public abstract class BaseAgent implements Agent {
    
    protected String id;
    protected String name;
    protected String description;
    protected int priority = 5; // Default priority
    protected boolean enabled = true;
    protected int executionInterval = 30; // Default 30 seconds
    
    protected BaseAgent(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    @Override
    public boolean shouldExecute(AgentContext context) {
        // Default implementation: check if enabled
        return enabled;
    }
    
    @Override
    public AgentResult execute(AgentContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Executing agent: {} with context: {}", getName(), context);
            
            if (!shouldExecute(context)) {
                return AgentResult.builder()
                        .agentId(getId())
                        .success(false)
                        .message("Agent execution skipped: shouldExecute returned false")
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
            
            AgentResult result = doExecute(context);
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            
            log.debug("Agent {} execution completed in {}ms with success: {}", 
                    getName(), result.getExecutionTimeMs(), result.isSuccess());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error executing agent: {}", getName(), e);
            return AgentResult.builder()
                    .agentId(getId())
                    .success(false)
                    .message("Agent execution failed: " + e.getMessage())
                    .error(e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * Subclasses implement this method to provide agent-specific logic
     */
    protected abstract AgentResult doExecute(AgentContext context);
}
