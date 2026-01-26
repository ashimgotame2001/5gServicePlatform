package com.service.devicemanagementservice.service;

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
 * Client service for interacting with Nokia Network as Code Device Management APIs
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
     * Swap SIM card
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> swapSim(Map<String, Object> swapRequest) {
        return webClient.post()
                .uri("/sim-swap/v0/swap")
                .bodyValue(swapRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Swap device
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> swapDevice(Map<String, Object> swapRequest) {
        return webClient.post()
                .uri("/device-swap/v0/swap")
                .bodyValue(swapRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }
}
