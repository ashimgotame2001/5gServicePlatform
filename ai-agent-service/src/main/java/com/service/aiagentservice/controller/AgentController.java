package com.service.aiagentservice.controller;

import com.service.aiagentservice.agent.Agent;
import com.service.aiagentservice.agent.model.AgentResult;
import com.service.aiagentservice.service.AgentOrchestrationService;
import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for AI Agent operations
 */
@Slf4j
@RestController
@RequestMapping("/ai-agents")
@RequiredArgsConstructor
public class AgentController {
    
    private final AgentOrchestrationService orchestrationService;
    
    /**
     * Execute all agents for a specific device
     */
    @PostMapping("/execute/{phoneNumber}")
    @MethodCode(value = "AE001", description = "Execute all agents for a device")
    public ResponseEntity<GlobalResponse> executeAgents(
            @PathVariable String phoneNumber) {
        log.info("Executing agents for phone number: {}", phoneNumber);
        
        List<AgentResult> results = orchestrationService.executeAgentsForDevice(phoneNumber);
        
        return ResponseHelper.successWithData("Agents executed successfully", results);
    }
    
    /**
     * Execute a specific agent for a device
     */
    @PostMapping("/execute/{agentId}/{phoneNumber}")
    @MethodCode(value = "AE006", description = "Execute specific agent for a device")
    public ResponseEntity<GlobalResponse> executeAgent(
            @PathVariable String agentId,
            @PathVariable String phoneNumber) {
        log.info("Executing agent {} for phone number: {}", agentId, phoneNumber);
        
        AgentResult result = orchestrationService.executeAgentForDevice(agentId, phoneNumber);
        
        return ResponseHelper.successWithData("Agent executed successfully", result);
    }
    
    /**
     * Get execution history for a device
     */
    @GetMapping("/history/{phoneNumber}")
    @MethodCode(value = "AE002", description = "Get agent execution history")
    public ResponseEntity<GlobalResponse> getExecutionHistory(
            @PathVariable String phoneNumber) {
        log.info("Getting execution history for phone number: {}", phoneNumber);
        
        List<AgentResult> history = orchestrationService.getExecutionHistory(phoneNumber);
        
        return ResponseHelper.successWithData("Execution history retrieved", history);
    }
    
    /**
     * Get all registered agents
     */
    @GetMapping("/agents")
    @MethodCode(value = "AE003", description = "List all agents")
    public ResponseEntity<GlobalResponse> getAllAgents() {
        log.info("Getting all registered agents");
        
        List<Map<String, Object>> agents = orchestrationService.getAllAgents().stream()
                .map(agent -> {
                    Map<String, Object> agentMap = new HashMap<>();
                    agentMap.put("id", agent.getId());
                    agentMap.put("name", agent.getName());
                    agentMap.put("description", agent.getDescription());
                    agentMap.put("priority", agent.getPriority());
                    agentMap.put("enabled", agent.isEnabled());
                    agentMap.put("executionInterval", agent.getExecutionInterval());
                    return agentMap;
                })
                .collect(Collectors.toList());
        
        return ResponseHelper.successWithData("Agents retrieved successfully", agents);
    }
    
    /**
     * Get agent by ID
     */
    @GetMapping("/agents/{agentId}")
    @MethodCode(value = "AE004", description = "Get agent details")
    public ResponseEntity<GlobalResponse> getAgent(@PathVariable String agentId) {
        log.info("Getting agent: {}", agentId);
        
        return orchestrationService.getAgent(agentId)
                .map(agent -> {
                    Map<String, Object> agentMap = new HashMap<>();
                    agentMap.put("id", agent.getId());
                    agentMap.put("name", agent.getName());
                    agentMap.put("description", agent.getDescription());
                    agentMap.put("priority", agent.getPriority());
                    agentMap.put("enabled", agent.isEnabled());
                    agentMap.put("executionInterval", agent.getExecutionInterval());
                    return ResponseHelper.successWithData("Agent retrieved successfully", agentMap);
                })
                .orElse(ResponseHelper.notFound("Agent not found: " + agentId));
    }
    
    /**
     * Enable/disable an agent
     */
    @PutMapping("/agents/{agentId}/enable")
    @MethodCode(value = "AE005", description = "Enable/disable agent")
    public ResponseEntity<GlobalResponse> enableAgent(
            @PathVariable String agentId,
            @RequestParam boolean enabled) {
        log.info("Setting agent {} enabled to {}", agentId, enabled);
        
        return orchestrationService.getAgent(agentId)
                .map(agent -> {
                    if (agent instanceof com.service.aiagentservice.agent.BaseAgent) {
                        ((com.service.aiagentservice.agent.BaseAgent) agent).setEnabled(enabled);
                        return ResponseHelper.successWithoutData("Agent " + (enabled ? "enabled" : "disabled") + " successfully");
                    }
                    return ResponseHelper.failure(org.springframework.http.HttpStatus.BAD_REQUEST, "Cannot modify agent state");
                })
                .orElse(ResponseHelper.notFound("Agent not found: " + agentId));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @MethodCode(value = "HC001", description = "Health check")
    public ResponseEntity<GlobalResponse> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "ai-agent-service");
        healthData.put("agentsCount", orchestrationService.getAllAgents().size());
        healthData.put("enabledAgentsCount", orchestrationService.getAllAgents().stream()
                .filter(Agent::isEnabled)
                .count());
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }
}
