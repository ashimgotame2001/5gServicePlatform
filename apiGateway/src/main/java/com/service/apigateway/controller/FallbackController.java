package com.service.apigateway.controller;

import com.service.apigateway.dto.GatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for CircuitBreaker fallback routes
 * This handles requests when downstream services are unavailable
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @PostMapping("/auth")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> authFallback() {
        log.warn("Auth service fallback triggered - service may be unavailable");
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("message", "Auth service is temporarily unavailable. Please try again later.");
        fallbackData.put("service", "auth-service");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GatewayResponse.failure(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Auth service is temporarily unavailable"
                ));
    }

    @GetMapping("/auth")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> authFallbackGet() {
        return authFallback();
    }

    @PostMapping("/connectivity")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> connectivityFallback() {
        log.warn("Connectivity service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GatewayResponse.failure(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Connectivity service is temporarily unavailable"
                ));
    }

    @PostMapping("/identification")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> identificationFallback() {
        log.warn("Identification service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GatewayResponse.failure(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Identification service is temporarily unavailable"
                ));
    }

    @PostMapping("/location")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> locationFallback() {
        log.warn("Location service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GatewayResponse.failure(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Location service is temporarily unavailable"
                ));
    }

    @PostMapping("/device")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> deviceFallback() {
        log.warn("Device management service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GatewayResponse.failure(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Device management service is temporarily unavailable"
                ));
    }

    @PostMapping("/ai-agents")
    public ResponseEntity<GatewayResponse<Map<String, Object>>> aiAgentsFallback() {
        log.warn("AI Agent service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GatewayResponse.failure(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "AI Agent service is temporarily unavailable"
                ));
    }
}
