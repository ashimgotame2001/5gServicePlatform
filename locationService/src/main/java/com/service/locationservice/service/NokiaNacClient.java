package com.service.locationservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Client service for interacting with Nokia Network as Code Location APIs
 */
@Service
public class NokiaNacClient {

    private final WebClient webClient;
    private final int retryAttempts;

    public NokiaNacClient(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts) {
        this.webClient = webClient;
        this.retryAttempts = retryAttempts;
    }

    /**
     * Retrieve location for a device
     * Based on Nokia RapidAPI location-retrieval endpoint
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> retrieveLocation(String phoneNumber, Integer maxAge) {
        Map<String, Object> requestBody = Map.of(
            "device", Map.of("phoneNumber", phoneNumber),
            "maxAge", maxAge != null ? maxAge : 60
        );
        
        return webClient.post()
                .uri("/location-retrieval/v0/retrieve")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Verify location
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> verifyLocation(Map<String, Object> locationRequest) {
        return webClient.post()
                .uri("/location-verification/v0/verify")
                .bodyValue(locationRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Get geo-fence alerts
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getGeofenceAlerts(String deviceId) {
        return webClient.get()
                .uri("/geofence/v0/alerts/{deviceId}", deviceId)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }
}
