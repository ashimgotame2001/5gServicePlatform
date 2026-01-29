package com.service.connectivityservice.client.impl;

import com.service.connectivityservice.client.NokiaNacNetworkSliceClient;
import com.service.shared.dto.request.CreateNetworkSliceSubscriptionDTO;
import com.service.shared.exception.GlobalException;
import com.service.shared.service.NokiaNacTokenManager;
import lombok.RequiredArgsConstructor;
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
 * Implementation of Nokia Network as Code API client for Network Slice subscriptions
 * Handles network slice subscription management
 */
@Slf4j
@Service
public class NokiaNacNetworkSliceClientImpl implements NokiaNacNetworkSliceClient {

    private static final String NETWORK_SLICE_SUBSCRIPTIONS_PATH = "/network-slice/v0/subscriptions";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;
    private final NokiaNacTokenManager tokenManager;

    @Value("${nokia.nac.base-url}")
    private String nokiaBaseUrl;
    @Value("${nokia.nac.rapidapi-host}")
    private String host;
    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNacNetworkSliceClientImpl(
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
                        log.warn("Retrying Nokia NAC Network Slice API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC Network Slice API retry exhausted after {} attempts", retryAttempts, failure);
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC Network Slice API retry exhausted after %d attempts: %s",
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
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> createNetworkSliceSubscription(CreateNetworkSliceSubscriptionDTO request) {
        log.info("Creating network slice subscription for device: {}", 
                request.getConfig() != null && 
                request.getConfig().getSubscriptionDetail() != null &&
                request.getConfig().getSubscriptionDetail().getDevice() != null ?
                request.getConfig().getSubscriptionDetail().getDevice().getPhoneNumber() : "unknown");

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.post()
                .uri(NETWORK_SLICE_SUBSCRIPTIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Network slice subscription created successfully: {}", result))
                .doOnError(error -> log.error("Failed to create network slice subscription", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to create network slice subscription: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getAllNetworkSliceSubscriptions() {
        log.debug("Fetching all network slice subscriptions");

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.get()
                .uri(NETWORK_SLICE_SUBSCRIPTIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved network slice subscriptions: {}", result))
                .doOnError(error -> log.error("Failed to get network slice subscriptions", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get network slice subscriptions: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getNetworkSliceSubscriptionById(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Subscription ID cannot be null or empty"
            ));
        }

        log.debug("Fetching network slice subscription with ID: {}", subscriptionId);

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.get()
                .uri(NETWORK_SLICE_SUBSCRIPTIONS_PATH + "/{id}", subscriptionId)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved network slice subscription: {}", result))
                .doOnError(error -> log.error("Failed to get network slice subscription with ID: {}", subscriptionId, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get network slice subscription: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> deleteNetworkSliceSubscription(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Subscription ID cannot be null or empty"
            ));
        }

        log.debug("Deleting network slice subscription with ID: {}", subscriptionId);

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.delete()
                .uri(NETWORK_SLICE_SUBSCRIPTIONS_PATH + "/{id}", subscriptionId)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Network slice subscription deleted successfully: {}", result))
                .doOnError(error -> log.error("Failed to delete network slice subscription: {}", subscriptionId, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to delete network slice subscription: " + throwable.getMessage(),
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
                    log.error("Nokia NAC Network Slice API error - Status: {}, Body: {}, Headers: {}",
                            statusCode, errorBody, response.headers().asHttpHeaders());

                    String errorMessage = String.format(
                            "Nokia NAC Network Slice API error [%s]: %s",
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
                    log.error("Failed to read error response body", throwable);
                    GlobalException exception = new GlobalException(
                            statusCode.value(),
                            String.format("Nokia NAC Network Slice API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
