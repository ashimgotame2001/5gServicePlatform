package com.service.aiagentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Client for internal service-to-service communication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalServiceClient {
    
    private final WebClient internalWebClient;
    
    @Value("${services.connectivity.base-url}")
    private String connectivityServiceUrl;
    
    @Value("${services.identification.base-url}")
    private String identificationServiceUrl;
    
    @Value("${services.location.base-url}")
    private String locationServiceUrl;
    
    @Value("${services.device-management.base-url}")
    private String deviceManagementServiceUrl;
    
    /**
     * Get connectivity status
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getConnectivityStatus(String phoneNumber) {
        return internalWebClient.get()
                .uri(connectivityServiceUrl + "/connectivity/status?phoneNumber={phoneNumber}", phoneNumber)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
    
    /**
     * Get QoS status
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getQoSStatus(String phoneNumber) {
        return internalWebClient.get()
                .uri(connectivityServiceUrl + "/connectivity/qos-status?phoneNumber={phoneNumber}", phoneNumber)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
    
    /**
     * Request QoS adjustment
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> requestQoSAdjustment(Map<String, Object> qosRequest) {
        return internalWebClient.post()
                .uri(connectivityServiceUrl + "/connectivity/qos-request")
                .bodyValue(qosRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
    
    /**
     * Get device status
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getDeviceStatus(String phoneNumber) {
        return internalWebClient.get()
                .uri(identificationServiceUrl + "/identification/device-status?phoneNumber={phoneNumber}", phoneNumber)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
    
    /**
     * Verify location
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> verifyLocation(String phoneNumber) {
        return internalWebClient.get()
                .uri(locationServiceUrl + "/location/verify?phoneNumber={phoneNumber}", phoneNumber)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
    
    /**
     * Swap SIM card
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> swapSim(Map<String, Object> swapRequest) {
        return internalWebClient.post()
                .uri(deviceManagementServiceUrl + "/device/swap-sim")
                .bodyValue(swapRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
    
    /**
     * Swap device
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> swapDevice(Map<String, Object> swapRequest) {
        return internalWebClient.post()
                .uri(deviceManagementServiceUrl + "/device/swap-device")
                .bodyValue(swapRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .onErrorReturn(new java.util.HashMap<>());
    }
}
