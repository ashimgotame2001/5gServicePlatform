package com.service.decisionengineservice.agent;

import com.service.decisionengineservice.agent.model.AgentContext;
import com.service.decisionengineservice.agent.model.AgentResult;

/**
 * Base interface for all AI agents in the 5G Service Platform.
 * Agents are autonomous entities that analyze network data and execute actions.
 */
public interface Agent {
    
    /**
     * Get the unique identifier for this agent
     */
    String getId();
    
    /**
     * Get the name of this agent
     */
    String getName();
    
    /**
     * Get the description of what this agent does
     */
    String getDescription();
    
    /**
     * Get the priority of this agent (higher = more important)
     */
    int getPriority();
    
    /**
     * Check if this agent is currently enabled
     */
    boolean isEnabled();
    
    /**
     * Execute the agent's main logic
     * @param context The current agent context with network data and state
     * @return The result of the agent's execution
     */
    AgentResult execute(AgentContext context);
    
    /**
     * Check if this agent should run based on current conditions
     * @param context The current agent context
     * @return true if agent should execute, false otherwise
     */
    boolean shouldExecute(AgentContext context);
    
    /**
     * Get the execution interval in seconds
     */
    int getExecutionInterval();
}
