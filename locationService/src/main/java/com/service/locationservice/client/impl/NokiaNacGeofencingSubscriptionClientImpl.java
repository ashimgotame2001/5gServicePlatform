package com.service.locationservice.client.impl;

import com.service.locationservice.client.NokiaNacGeofencingSubscriptionClient;
import com.service.shared.dto.request.CreateGeofencingSubscriptionDTO;
import com.service.shared.service.NokiaNacTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Implementation of Nokia NAC Geofencing Subscription Client
 * Based on CAMARA Project geofencing subscription API
 */
@Service
@Slf4j
public class NokiaNacGeofencingSubscriptionClientImpl implements NokiaNacGeofencingSubscriptionClient {

    private static final String BASE_PATH = "https://geofencing-subscriptions.p-eu.rapidapi.com/v0.3/";
    private static final String CREATE_SUBSCRIPTION_PATH = BASE_PATH + "subscriptions";
    private static final String GET_ALL_SUBSCRIPTIONS_PATH = BASE_PATH + "subscriptions";
    private static final String GET_SUBSCRIPTION_BY_ID_PATH = BASE_PATH + "subscriptions/";
    private static final String DELETE_SUBSCRIPTION_PATH = BASE_PATH + "subscriptions/";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    private static final String HOST = "geofencing-subscriptions.p-eu.rapidapi.com";

    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;
    private final NokiaNacTokenManager tokenManager;

    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNacGeofencingSubscriptionClientImpl(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.timeout:30000}") int timeoutMs,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts,
            NokiaNacTokenManager tokenManager
    ) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = createRetrySpec(retryAttempts);
        this.tokenManager = tokenManager;
    }

    private Retry createRetrySpec(int retryAttempts) {
        return Retry.fixedDelay(retryAttempts, RETRY_DELAY)
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying Nokia NAC Geofencing Subscription API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC Geofencing Subscription API retry exhausted after {} attempts", retryAttempts, failure);
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC Geofencing Subscription API retry exhausted after %d attempts: %s",
                                    retryAttempts, failure.getMessage()),
                            failure);
                });
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof IllegalArgumentException ||
                throwable instanceof DecodingException) {
            return false;
        }

        if (throwable instanceof WebClientResponseException webClientException) {
            HttpStatusCode statusCode = webClientException.getStatusCode();
            if (statusCode != null) {
                return statusCode.is5xxServerError() ||
                        statusCode.value() == HttpStatus.REQUEST_TIMEOUT.value() ||
                        statusCode.value() == HttpStatus.SERVICE_UNAVAILABLE.value();
            }
        }

        return !(throwable instanceof DecodingException);
    }

    @Override
    public Mono<Map<String, Object>> createGeofencingSubscription(CreateGeofencingSubscriptionDTO request) {
        log.info("Creating geofencing subscription for device: {}",
                request.getConfig() != null &&
                        request.getConfig().getSubscriptionDetail() != null &&
                        request.getConfig().getSubscriptionDetail().getDevice() != null ?
                        request.getConfig().getSubscriptionDetail().getDevice().getPhoneNumber() : "unknown");

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();

        return webClient.post()
                .uri(CREATE_SUBSCRIPTION_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", HOST)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Geofencing subscription created successfully: {}", result))
                .doOnError(error -> log.error("Failed to create geofencing subscription", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof com.service.shared.exception.GlobalException) {
                        return throwable;
                    }
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to create geofencing subscription: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    public Mono<Map<String, Object>> getAllGeofencingSubscriptions() {
        log.info("Retrieving all geofencing subscriptions");

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();

        return webClient.get()
                .uri(GET_ALL_SUBSCRIPTIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", HOST)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Retrieved all geofencing subscriptions successfully: {}", result))
                .doOnError(error -> log.error("Failed to get all geofencing subscriptions", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof com.service.shared.exception.GlobalException) {
                        return throwable;
                    }
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get all geofencing subscriptions: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    public Mono<Map<String, Object>> getGeofencingSubscriptionById(String subscriptionId) {
        log.info("Retrieving geofencing subscription by ID: {}", subscriptionId);

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();

        return webClient.get()
                .uri(GET_SUBSCRIPTION_BY_ID_PATH + subscriptionId)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", HOST)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Retrieved geofencing subscription by ID successfully: {}", result))
                .doOnError(error -> log.error("Failed to get geofencing subscription by ID: {}", subscriptionId, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof com.service.shared.exception.GlobalException) {
                        return throwable;
                    }
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get geofencing subscription by ID: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    public Mono<Map<String, Object>> deleteGeofencingSubscription(String subscriptionId) {
        log.info("Deleting geofencing subscription by ID: {}", subscriptionId);

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();

        return webClient.delete()
                .uri(DELETE_SUBSCRIPTION_PATH + subscriptionId)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", HOST)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Deleted geofencing subscription successfully: {}", result))
                .doOnError(error -> log.error("Failed to delete geofencing subscription: {}", subscriptionId, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof com.service.shared.exception.GlobalException) {
                        return throwable;
                    }
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to delete geofencing subscription: " + throwable.getMessage(),
                            throwable);
                });
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode statusCode = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("No error body")
                .flatMap(errorBody -> {
                    log.error("Nokia NAC Geofencing Subscription API error - Status: {}, Body: {}, Headers: {}",
                            statusCode, errorBody, response.headers().asHttpHeaders());

                    String errorMessage = String.format(
                            "Nokia NAC Geofencing Subscription API error [%s]: %s",
                            statusCode,
                            errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody
                    );

                    com.service.shared.exception.GlobalException exception = new com.service.shared.exception.GlobalException(
                            statusCode.value(),
                            errorMessage
                    );
                    return Mono.<Throwable>error(exception);
                })
                .onErrorResume(throwable -> {
                    // If we can't read the error body, still return an error
                    log.error("Failed to read error response body", throwable);
                    com.service.shared.exception.GlobalException exception = new com.service.shared.exception.GlobalException(
                            statusCode.value(),
                            String.format("Nokia NAC Geofencing Subscription API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
