package com.service.connectivityservice.client.impl;

import com.service.connectivityservice.client.NokiaNacQosClient;
import com.service.shared.dto.request.CreateSessionRequestDTO;
import com.service.shared.dto.request.DeviceRequestDTO;
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
 * Implementation of Nokia Network as Code API client
 * Handles Quality of Service on Demand (QoD) session management
 */
@Slf4j
@Service
public class NokiaNacQosClientImpl implements NokiaNacQosClient {

    private static final String QOD_SESSIONS_PATH = "/qod/v0/sessions";
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

    public NokiaNacQosClientImpl(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.timeout:30000}") int timeoutMs,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts,
            NokiaNacTokenManager tokenManager
    ) {
        this.webClient = webClient;
        this.tokenManager = tokenManager;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = createRetrySpec(retryAttempts);
    }

    private Retry createRetrySpec(int retryAttempts) {
        return Retry.fixedDelay(retryAttempts, RETRY_DELAY)
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying Nokia NAC API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC API retry exhausted after {} attempts", retryAttempts, failure);
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC API retry exhausted after %d attempts: %s",
                                    retryAttempts, failure.getMessage()),
                            failure);
                });
    }


    private boolean isRetryableError(Throwable throwable) {
        // Don't retry on client errors (4xx), deserialization errors, or illegal arguments
        if (throwable instanceof IllegalArgumentException ||
                throwable instanceof DecodingException) {
            return false;
        }

        if (throwable instanceof WebClientResponseException webClientException) {
            HttpStatusCode statusCode = webClientException.getStatusCode();
            if (statusCode != null) {
                // Only retry on server errors (5xx) and specific network errors
                return statusCode.is5xxServerError() ||
                        statusCode.value() == HttpStatus.REQUEST_TIMEOUT.value() ||
                        statusCode.value() == HttpStatus.SERVICE_UNAVAILABLE.value();
            }
        }

        // Retry on network/connection errors, but not on deserialization or validation errors
        return !(throwable instanceof DecodingException);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> createSession(CreateSessionRequestDTO requestDTO) {


        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.post()
                .uri(QOD_SESSIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(requestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("QoD session created successfully: {}", result))
                .doOnError(error -> log.error("Failed to create QoD session", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to create QoD session: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getSessions() {
        log.debug("Fetching all QoD sessions");

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.get()
                .uri(QOD_SESSIONS_PATH)
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
                .doOnSuccess(result -> log.debug("Retrieved QoD sessions: {}", result))
                .doOnError(error -> log.error("Failed to get QoD sessions", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get QoD sessions: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> retrieveSessions(DeviceRequestDTO request) {


        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.post()
                .uri(QOD_SESSIONS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Object.class)
                .map(response -> {
                    // Handle both array and object responses
                    Map<String, Object> result = new java.util.HashMap<>();
                    if (response instanceof java.util.List) {
                        // If response is an array, wrap it in a map
                        result.put("sessions", response);
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
                .doOnSuccess(result -> log.info("Retrieved QoD sessions successfully: {}", result))
                .doOnError(error -> log.error("Failed to retrieve QoD sessions", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to retrieve QoD sessions: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Session ID cannot be null or empty"
            ));
        }

        log.debug("Fetching QoD session with ID: {}", sessionId);

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.get()
                .uri(QOD_SESSIONS_PATH + "/{id}", sessionId)
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
                .doOnSuccess(result -> log.debug("Retrieved QoD session: {}", result))
                .doOnError(error -> log.error("Failed to get QoD session with ID: {}", sessionId, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get QoD session: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> postRequest(String endpoint, Object requestBody) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Endpoint cannot be null or empty"
            ));
        }
        if (requestBody == null) {
            return Mono.error(new GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Request body cannot be null"
            ));
        }

        log.debug("Making POST request to endpoint: {} with body: {}", endpoint, requestBody);

        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return webClient.post()
                .uri(endpoint)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("POST request successful to endpoint: {}", endpoint))
                .doOnError(error -> log.error("Failed to make POST request to endpoint: {}", endpoint, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to make POST request to " + endpoint + ": " + throwable.getMessage(),
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
