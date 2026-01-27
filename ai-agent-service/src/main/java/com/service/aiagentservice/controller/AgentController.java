package com.service.aiagentservice.controller;

import com.service.aiagentservice.agent.Agent;
import com.service.aiagentservice.agent.model.AgentResult;
import com.service.aiagentservice.service.AgentOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<com.service.shared.dto.GlobalResponse> executeAgents(
            @PathVariable String phoneNumber) {
        log.info("Executing agents for phone number: {}", phoneNumber);
        
        List<AgentResult> results = orchestrationService.executeAgentsForDevice(phoneNumber);
        
        return com.service.shared.util.ResponseHelper.successWithData(results);
    }
    
    /**
     * Get execution history for a device
     */
    @GetMapping("/history/{phoneNumber}")
    public ResponseEntity<com.service.shared.dto.GlobalResponse> getExecutionHistory(
            @PathVariable String phoneNumber) {
        log.info("Getting execution history for phone number: {}", phoneNumber);
        
        List<AgentResult> history = orchestrationService.getExecutionHistory(phoneNumber);
        
        return com.service.shared.util.ResponseHelper.successWithData(history);
    }
    
    /**
     * Get all registered agents
     */
    @GetMapping("/agents")
    public ResponseEntity<GlobalResponse<List<Map<String, Object>>>> getAllAgents() {
        log.info("Getting all registered agents");
        
        List<Map<String, Object>> agents = orchestrationService.getAllAgents().stream()
                .map(agent -> {
                    Map<String, Object> agentMap = new java.util.HashMap<>();
                    agentMap.put("id", agent.getId());
                    agentMap.put("name", agent.getName());
                    agentMap.put("description", agent.getDescription());
                    agentMap.put("priority", agent.getPriority());
                    agentMap.put("enabled", agent.isEnabled());
                    agentMap.put("executionInterval", agent.getExecutionInterval());
                    return agentMap;
                })
                .collect(Collectors.toList());
        
        return ResponseHelper.successWithData(agents);
    }
    
    /**
     * Get agent by ID
     */
    @GetMapping("/agents/{agentId}")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> getAgent(@PathVariable String agentId) {
        log.info("Getting agent: {}", agentId);
        
        return orchestrationService.getAgent(agentId)
                .map(agent -> {
                    Map<String, Object> agentMap = new java.util.HashMap<>();
                    agentMap.put("id", agent.getId());
                    agentMap.put("name", agent.getName());
                    agentMap.put("description", agent.getDescription());
                    agentMap.put("priority", agent.getPriority());
                    agentMap.put("enabled", agent.isEnabled());
                    agentMap.put("executionInterval", agent.getExecutionInterval());
                    return ResponseHelper.<Map<String, Object>>successWithData(agentMap);
                })
                .orElse(ResponseHelper.<Map<String, Object>>notFound("Agent not found: " + agentId));
    }
    
    /**
     * Enable/disable an agent
     */
    @PutMapping("/agents/{agentId}/enable")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> enableAgent(
            @PathVariable String agentId,
            @RequestParam boolean enabled) {
        log.info("Setting agent {} enabled to {}", agentId, enabled);
        
        return orchestrationService.getAgent(agentId)
                .map(agent -> {
                    if (agent instanceof com.service.aiagentservice.agent.BaseAgent) {
                        ((com.service.aiagentservice.agent.BaseAgent) agent).setEnabled(enabled);
                        return ResponseHelper.<Map<String, Object>>success("Agent " + (enabled ? "enabled" : "disabled"));
                    }
                    return ResponseHelper.<Map<String, Object>>error("Cannot modify agent state");
                })
                .orElse(ResponseHelper.<Map<String, Object>>notFound("Agent not found: " + agentId));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = new java.util.HashMap<>();
        healthData.put("status", "UP");
        healthData.put("agentsCount", orchestrationService.getAllAgents().size());
        healthData.put("enabledAgentsCount", orchestrationService.getAllAgents().stream()
                .filter(Agent::isEnabled)
                .count());
        return ResponseHelper.successWithData(healthData);
    }
}
