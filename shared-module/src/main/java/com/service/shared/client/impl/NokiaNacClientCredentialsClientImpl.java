package com.service.shared.client.impl;

import com.service.shared.client.NokiaNacClientCredentialsClient;
import com.service.shared.dto.NokiaNacClientCredentialsDTO;
import com.service.shared.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

/**
 * Implementation of Nokia NAC Client Credentials Client
 * Retrieves client_id and client_secret from Nokia NAC authorization server
 */
@Slf4j
@Component
public class NokiaNacClientCredentialsClientImpl implements NokiaNacClientCredentialsClient {

    private static final String CLIENT_CREDENTIALS_ENDPOINT = "https://nac-authorization-server.p-eu.rapidapi.com/auth/clientcredentials";
    private static final String RAPIDAPI_HOST = "nac-authorization-server.nokia.rapidapi.com";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    
    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;
    
    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;
    
    public NokiaNacClientCredentialsClientImpl(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.timeout:30000}") int timeoutMs,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts
    ) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = createRetrySpec(retryAttempts);
    }
    
    private Retry createRetrySpec(int retryAttempts) {
        return Retry.fixedDelay(retryAttempts, RETRY_DELAY)
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying Nokia NAC Client Credentials API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC Client Credentials API retry exhausted after {} attempts", retryAttempts, failure);
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC Client Credentials API retry exhausted after %d attempts: %s",
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
    public Mono<NokiaNacClientCredentialsDTO> getClientCredentials() {
        log.info("Retrieving client credentials from Nokia NAC authorization server");
        
        return webClient.get()
                .uri(URI.create(CLIENT_CREDENTIALS_ENDPOINT))
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(NokiaNacClientCredentialsDTO.class)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> {
                    if (result != null) {
                        boolean hasClientId = result.getClientId() != null && !result.getClientId().trim().isEmpty();
                        boolean hasClientSecret = result.getClientSecret() != null && !result.getClientSecret().trim().isEmpty();
                        
                        if (hasClientId && hasClientSecret) {
                            log.info("Successfully retrieved client credentials (client_id length: {}, client_secret length: {})", 
                                    result.getClientId().length(), result.getClientSecret().length());
                        } else {
                            log.error("Retrieved client credentials but values are empty - client_id empty: {}, client_secret empty: {}. Full response: {}", 
                                    !hasClientId, !hasClientSecret, result);
                        }
                    } else {
                        log.error("Retrieved client credentials but response is null");
                    }
                })
                .doOnError(error -> log.error("Failed to retrieve client credentials", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to retrieve client credentials: " + throwable.getMessage(),
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
                    log.error("Nokia NAC Client Credentials API error - Status: {}, Body: {}, Headers: {}",
                            statusCode, errorBody, response.headers().asHttpHeaders());
                    
                    String errorMessage = String.format(
                            "Nokia NAC Client Credentials API error [%s]: %s",
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
                            String.format("Nokia NAC Client Credentials API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
