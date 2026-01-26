package com.service.connectivityservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Client service for interacting with Nokia Network as Code APIs
 * This service handles all HTTP calls to Nokia's Programmable Connectivity APIs
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
     * Request QoS adjustment for connectivity
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> requestQoS(Map<String, Object> qosRequest) {
        return webClient.post()
                .uri("/connectivity/v0/qos")
                .bodyValue(qosRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Get connectivity status
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getConnectivityStatus(String requestId) {
        return webClient.get()
                .uri("/connectivity/v0/status/{requestId}", requestId)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Generic method for making POST requests to Nokia APIs
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> postRequest(String endpoint, Object requestBody) {
        return webClient.post()
                .uri(endpoint)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Generic method for making GET requests to Nokia APIs
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getRequest(String endpoint, Map<String, String> queryParams) {
        var uriSpec = webClient.get().uri(uriBuilder -> {
            var builder = uriBuilder.path(endpoint);
            if (queryParams != null) {
                queryParams.forEach(builder::queryParam);
            }
            return builder.build();
        });

        return uriSpec
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(2)))
                .timeout(Duration.ofSeconds(30));
    }
}
