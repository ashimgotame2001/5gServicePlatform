package com.service.decisionengineservice.service;

import com.service.decisionengineservice.agent.Agent;
import com.service.decisionengineservice.agent.model.AgentContext;
import com.service.decisionengineservice.agent.model.AgentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service that orchestrates and coordinates multiple AI agents
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrationService {
    
    private final List<Agent> agents;
    private final NetworkDataCollectionService dataCollectionService;
    
    @Value("${ai.agents.enabled:true}")
    private boolean agentsEnabled;
    
    @Value("${ai.agents.max-concurrent-agents:10}")
    private int maxConcurrentAgents;
    
    // Store agent execution results
    private final Map<String, List<AgentResult>> executionHistory = new ConcurrentHashMap<>();
    
    /**
     * Execute all agents for a specific phone number
     */
    public List<AgentResult> executeAgentsForDevice(String phoneNumber) {
        if (!agentsEnabled) {
            log.debug("Agents are disabled, skipping execution");
            return Collections.emptyList();
        }
        
        log.info("Executing agents for device: {}", phoneNumber);
        
        // Collect network data
        com.service.decisionengineservice.agent.model.NetworkData networkData = dataCollectionService
                .collectNetworkData(phoneNumber)
                .block();
        
        if (networkData == null) {
            log.warn("Failed to collect network data for {}", phoneNumber);
            return Collections.emptyList();
        }
        
        // Create agent context
        AgentContext context = AgentContext.builder()
                .phoneNumber(phoneNumber)
                .networkData(networkData)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        // Get enabled agents sorted by priority
        List<Agent> enabledAgents = agents.stream()
                .filter(Agent::isEnabled)
                .sorted(Comparator.comparing(Agent::getPriority).reversed())
                .limit(maxConcurrentAgents)
                .collect(Collectors.toList());
        
        log.info("Executing {} agents for device {}", enabledAgents.size(), phoneNumber);
        
        // Execute agents (can be parallelized in future)
        List<AgentResult> results = new ArrayList<>();
        for (Agent agent : enabledAgents) {
            try {
                if (agent.shouldExecute(context)) {
                    AgentResult result = agent.execute(context);
                    results.add(result);
                    
                    // Store previous results for coordination
                    context.getPreviousResults().put(agent.getId(), result);
                }
            } catch (Exception e) {
                log.error("Error executing agent: {}", agent.getId(), e);
                results.add(AgentResult.builder()
                        .agentId(agent.getId())
                        .success(false)
                        .error(e.getMessage())
                        .build());
            }
        }
        
        // Store execution history
        executionHistory.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).addAll(results);
        
        log.info("Completed execution of {} agents for device {}", results.size(), phoneNumber);
        
        return results;
    }
    
    /**
     * Get execution history for a device
     */
    public List<AgentResult> getExecutionHistory(String phoneNumber) {
        return executionHistory.getOrDefault(phoneNumber, Collections.emptyList());
    }
    
    /**
     * Get all registered agents
     */
    public List<Agent> getAllAgents() {
        return new ArrayList<>(agents);
    }
    
    /**
     * Get agent by ID
     */
    public Optional<Agent> getAgent(String agentId) {
        return agents.stream()
                .filter(agent -> agent.getId().equals(agentId))
                .findFirst();
    }
    
    /**
     * Execute a specific agent for a device
     */
    public AgentResult executeAgentForDevice(String agentId, String phoneNumber) {
        if (!agentsEnabled) {
            log.debug("Agents are disabled, skipping execution");
            return AgentResult.builder()
                    .agentId(agentId)
                    .success(false)
                    .error("Agents are disabled")
                    .build();
        }
        
        log.info("Executing agent {} for device: {}", agentId, phoneNumber);
        
        Optional<Agent> agentOpt = getAgent(agentId);
        if (agentOpt.isEmpty()) {
            log.warn("Agent not found: {}", agentId);
            return AgentResult.builder()
                    .agentId(agentId)
                    .success(false)
                    .error("Agent not found: " + agentId)
                    .build();
        }
        
        Agent agent = agentOpt.get();
        if (!agent.isEnabled()) {
            log.warn("Agent {} is disabled", agentId);
            return AgentResult.builder()
                    .agentId(agentId)
                    .success(false)
                    .error("Agent is disabled")
                    .build();
        }
        
        // Collect network data
        com.service.decisionengineservice.agent.model.NetworkData networkData = dataCollectionService
                .collectNetworkData(phoneNumber)
                .block();
        
        if (networkData == null) {
            log.warn("Failed to collect network data for {}", phoneNumber);
            return AgentResult.builder()
                    .agentId(agentId)
                    .success(false)
                    .error("Failed to collect network data")
                    .build();
        }
        
        // Create agent context
        AgentContext context = AgentContext.builder()
                .phoneNumber(phoneNumber)
                .networkData(networkData)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        try {
            if (agent.shouldExecute(context)) {
                AgentResult result = agent.execute(context);
                
                // Store execution history
                executionHistory.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(result);
                
                log.info("Completed execution of agent {} for device {}", agentId, phoneNumber);
                return result;
            } else {
                log.debug("Agent {} should not execute for device {}", agentId, phoneNumber);
                return AgentResult.builder()
                        .agentId(agentId)
                        .success(false)
                        .message("Agent should not execute for this context")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error executing agent: {}", agentId, e);
            AgentResult errorResult = AgentResult.builder()
                    .agentId(agentId)
                    .success(false)
                    .error(e.getMessage())
                    .build();
            
            // Store error in history
            executionHistory.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(errorResult);
            
            return errorResult;
        }
    }
    
    /**
     * Scheduled execution for monitoring devices
     * This can be configured to run for specific devices or all active devices
     */
    @Scheduled(fixedDelayString = "${ai.agents.execution-interval:30}000")
    public void scheduledAgentExecution() {
        if (!agentsEnabled) {
            return;
        }
        
        log.debug("Scheduled agent execution triggered");
        
        // In a real implementation, this would iterate over active devices
        // For now, this is a placeholder that can be extended
        // Example: executeAgentsForDevice("+1234567890");
    }
}
