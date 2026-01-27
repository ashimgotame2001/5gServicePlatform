package com.service.shared.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;


@Service
public class InternalServiceClient {

    private final WebClient internalWebClient;

    public InternalServiceClient(@Qualifier("internalWebClient") WebClient internalWebClient) {
        this.internalWebClient = internalWebClient;
    }


    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> callService(String serviceUrl, String endpoint, Object requestBody) {
        return internalWebClient.post()
                .uri(serviceUrl + endpoint)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map);
    }


    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getFromService(String serviceUrl, String endpoint) {
        return internalWebClient.get()
                .uri(serviceUrl + endpoint)
                .retrieve()
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map);
    }
}
