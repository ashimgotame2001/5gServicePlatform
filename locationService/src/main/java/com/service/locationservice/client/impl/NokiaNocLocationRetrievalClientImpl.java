package com.service.locationservice.client.impl;

import com.service.locationservice.client.NokiaNocLocationRetrievalClient;
import com.service.shared.dto.request.LocationRetrievalDTO;
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

@Service
@Slf4j
public class NokiaNocLocationRetrievalClientImpl implements NokiaNocLocationRetrievalClient {

    private static final String BASE_URL = "https://location-retrieval.p-eu.rapidapi.com";
    private static final String LOCATION_RETRIEVAL_PATH = BASE_URL + "/v0/retrieve";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    // RapidAPI host header format: {service}.nokia.rapidapi.com or {service}.p-eu.rapidapi.com
    private static final String HOST = "location-retrieval.nokia.rapidapi.com";


    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;
    private final NokiaNacTokenManager tokenManager;


    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNocLocationRetrievalClientImpl(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.timeout:30000}") int timeoutMs,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts,
            com.service.shared.util.ClientUtil clientUtil,
            NokiaNacTokenManager tokenManager
    ) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = createRetrySpec(retryAttempts);
        this.tokenManager = tokenManager;
    }

    public Retry createRetrySpec(int retryAttempts) {
        return Retry.fixedDelay(retryAttempts, RETRY_DELAY)
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying Nokia NAC API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC API retry exhausted after {} attempts", retryAttempts, failure);
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC API retry exhausted after %d attempts: %s",
                                    retryAttempts, failure.getMessage()),
                            failure);
                });
    }

    public boolean isRetryableError(Throwable throwable) {
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
    public Mono<Map<String, Object>> retriveLocation(LocationRetrievalDTO request) {
        // Use mutate() to create a new WebClient instance without default headers
        // This ensures we use the correct host header for this specific endpoint
        WebClient locationWebClient = webClient.mutate()
                .baseUrl("") // Clear base URL since we're using absolute URI
                .defaultHeaders(headers -> {
                    headers.remove("x-rapidapi-key");
                    headers.remove("x-rapidapi-host");
                })
                .build();
        
        // Get OAuth2 access token
        String accessToken = tokenManager.getAccessToken();
        
        return locationWebClient.post()
                .uri(LOCATION_RETRIEVAL_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", HOST)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Map.class)
                .cast(Map.class)
                .map(map -> (Map<String, Object>) map)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Retrieved location successfully: {}", result))
                .doOnError(error -> log.error("Failed to retrieve location", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof com.service.shared.exception.GlobalException) {
                        return throwable;
                    }
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to retrieve location: " + throwable.getMessage(),
                            throwable);
                });
    }
    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode statusCode = response.statusCode();

        return response.bodyToMono(String.class)
                .defaultIfEmpty("No error body")
                .flatMap(errorBody -> {
                    log.error("Nokia NAC Location Retrieval API error - Status: {}, Body: {}, Request Headers: {}, Response Headers: {}",
                            statusCode, errorBody, 
                            response.request().getHeaders(),
                            response.headers().asHttpHeaders());

                    // Log full error body for 403 errors to help diagnose authentication issues
                    if (statusCode.value() == 403) {
                        log.error("403 Forbidden - Full error response: {}", errorBody);
                        log.error("API Key used: {} (length: {})", 
                                apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null",
                                apiKey != null ? apiKey.length() : 0);
                        log.error("Host header used: {}", HOST);
                    }

                    String errorMessage = String.format(
                            "Nokia NAC Location Retrieval API error [%s]: %s",
                            statusCode,
                            errorBody.length() > 500 ? errorBody.substring(0, 500) + "..." : errorBody
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
                            String.format("Nokia NAC Location Retrieval API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }

}
