package com.service.identificationservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Client service for interacting with Nokia Network as Code Identification APIs
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
     * Verify phone number
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> verifyNumber(Map<String, Object> verificationRequest) {
        return webClient.post()
                .uri("/number-verification/v0/verify")
                .bodyValue(verificationRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Perform KYC check
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> performKycCheck(Map<String, Object> kycRequest) {
        return webClient.post()
                .uri("/kyc/v0/check")
                .bodyValue(kycRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Get device status
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getDeviceStatus(String deviceId) {
        return webClient.get()
                .uri("/device-status/v0/status/{deviceId}", deviceId)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }
}
