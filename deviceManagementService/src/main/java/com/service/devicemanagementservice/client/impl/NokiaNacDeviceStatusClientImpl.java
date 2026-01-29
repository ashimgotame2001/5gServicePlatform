package com.service.devicemanagementservice.client.impl;

import com.service.devicemanagementservice.client.NokiaNacDeviceStatusClient;
import com.service.shared.dto.CreateDeviceStatusSubscriptionDTO;
import com.service.shared.dto.DeviceConnectivityStatusDTO;
import com.service.shared.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;


@Slf4j
@Service
public class NokiaNacDeviceStatusClientImpl implements NokiaNacDeviceStatusClient {

    private static final String DEVICE_STATUS_PATH = "https://device-status.p-eu.rapidapi.com/";
    private static final String CONNECTIVITY_STATUS_PATH = DEVICE_STATUS_PATH + "connectivity";
    private static final String ROAMING_STATUS_PATH = DEVICE_STATUS_PATH + "roaming";
    private static final String SUBSCRIPTIONS_PATH = DEVICE_STATUS_PATH + "subscriptions";
    private static final String host ="device-status.nokia.rapidapi.com";


    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;


    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNacDeviceStatusClientImpl(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.timeout:30000}") int timeoutMs,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts, com.service.shared.util.ClientUtil clientUtil
    ) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = clientUtil.createRetrySpec(retryAttempts);
    }



    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getDeviceConnectivityStatus(DeviceConnectivityStatusDTO status) {
        if (status == null) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Device connectivity status request cannot be null"
            ));
        }

        log.debug("Fetching device connectivity status for device: {}", status.getPhoneNumber());

        return webClient.post()
                .uri(CONNECTIVITY_STATUS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .bodyValue(status)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Retrieved device connectivity status successfully: {}", result))
                .doOnError(error -> log.error("Failed to get device connectivity status", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get device connectivity status: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getDeviceRoamingStatus(DeviceConnectivityStatusDTO status) {
        if (status == null) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Device roaming status request cannot be null"
            ));
        }

        log.debug("Fetching device roaming status for device: {}", status.getPhoneNumber());

        return webClient.post()
                .uri(ROAMING_STATUS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .bodyValue(status)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Retrieved device roaming status successfully: {}", result))
                .doOnError(error -> log.error("Failed to get device roaming status", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get device roaming status: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getAllSubscription() {
        log.debug("Fetching all device status subscriptions");

        return webClient.get()
                .uri(SUBSCRIPTIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Object.class)
                .map(response -> {
                    // Handle both array and object responses
                    Map<String, Object> result = new java.util.HashMap<>();
                    if (response instanceof java.util.List) {
                        // If response is an array, wrap it in a map
                        result.put("subscriptions", response);
                        result.put("count", ((java.util.List<?>) response).size());
                    } else if (response instanceof Map) {
                        // If response is already a map, use it directly
                        result.putAll((Map<String, Object>) response);
                    } else {
                        // For any other type, wrap it
                        result.put("data", response);
                    }
                    return result;
                })
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved all subscriptions: {}", result))
                .doOnError(error -> log.error("Failed to get all subscriptions", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get all subscriptions: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> createDeviceStatusSubscription(CreateDeviceStatusSubscriptionDTO request) {
        if (request == null) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Create subscription request cannot be null"
            ));
        }

        log.debug("Creating device status subscription for device: {}", 
                request.getSubscriptionDetail() != null && 
                request.getSubscriptionDetail().getDevice() != null ?
                request.getSubscriptionDetail().getDevice().getPhoneNumber() : "unknown");

        return webClient.post()
                .uri(SUBSCRIPTIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Created device status subscription successfully: {}", result))
                .doOnError(error -> log.error("Failed to create device status subscription", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to create device status subscription: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getSubscriptionById(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Subscription ID cannot be null or empty"
            ));
        }

        log.debug("Fetching subscription with ID: {}", subscriptionId);

        return webClient.get()
                .uri(SUBSCRIPTIONS_PATH + "/{id}", subscriptionId)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved subscription: {}", result))
                .doOnError(error -> log.error("Failed to get subscription with ID: {}", subscriptionId, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get subscription: " + throwable.getMessage(),
                            throwable);
                });
    }

    /**
     * Handle HTTP error responses with detailed logging
     */
    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode statusCode = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("No error body")
                .flatMap(errorBody -> {
                    log.error("Nokia NAC API error - Status: {}, Body: {}, Headers: {}",
                            statusCode, errorBody, response.headers().asHttpHeaders());

                    String errorMessage = String.format(
                            "Nokia NAC API error [%s]: %s",
                            statusCode,
                            errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody
                    );

                    GlobalException exception = new GlobalException(
                            statusCode.value(),
                            errorMessage
                    );
                    return Mono.<Throwable>error(exception);
                })
                .onErrorResume(throwable -> {
                    // If we can't read the error body, still return an error
                    log.error("Failed to read error response body", throwable);
                    GlobalException exception = new GlobalException(
                            statusCode.value(),
                            String.format("Nokia NAC API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
