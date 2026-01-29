package com.service.shared.client.impl;

import com.service.shared.client.NokiaNacMetadataClient;
import com.service.shared.dto.NokiaNacMetadataDTO;
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
 * Implementation of Nokia NAC Metadata Client
 * Handles OpenID configuration and OAuth authorization server metadata endpoints
 */
@Slf4j
@Component
public class NokiaNacMetadataClientImpl implements NokiaNacMetadataClient {

    private static final String METADATA_BASE_URL = "https://well-known-metadata.p-eu.rapidapi.com";
    private static final String OPENID_CONFIG_PATH = "/openid-configuration";
    private static final String SECURITY_TXT_PATH = "/security.txt";
    private static final String OAUTH_AUTH_SERVER_PATH = "/oauth-authorization-server";
    private static final String RAPIDAPI_HOST = "well-known-metadata.nokia.rapidapi.com";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;

    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNacMetadataClientImpl(
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
                        log.warn("Retrying Nokia NAC Metadata API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC Metadata API retry exhausted after {} attempts", retryAttempts, failure);
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC Metadata API retry exhausted after %d attempts: %s",
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
    public Mono<NokiaNacMetadataDTO> getOpenIdConfiguration() {
        log.debug("Fetching OpenID configuration metadata");

        return webClient.get()
                .uri(URI.create(METADATA_BASE_URL + OPENID_CONFIG_PATH))
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(NokiaNacMetadataDTO.class)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved OpenID configuration: {}", result))
                .doOnError(error -> log.error("Failed to get OpenID configuration", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get OpenID configuration: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    public Mono<String> getSecurityTxt() {
        log.debug("Fetching security.txt");

        return webClient.get()
                .uri(URI.create(METADATA_BASE_URL + SECURITY_TXT_PATH))
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(String.class)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved security.txt: {}", result))
                .doOnError(error -> log.error("Failed to get security.txt", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get security.txt: " + throwable.getMessage(),
                            throwable);
                });
    }

    @Override
    public Mono<NokiaNacMetadataDTO> getOAuthAuthorizationServer() {
        log.debug("Fetching OAuth authorization server metadata");

        return webClient.get()
                .uri(URI.create(METADATA_BASE_URL + OAUTH_AUTH_SERVER_PATH))
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", RAPIDAPI_HOST)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(NokiaNacMetadataDTO.class)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.debug("Retrieved OAuth authorization server metadata: {}", result))
                .doOnError(error -> log.error("Failed to get OAuth authorization server metadata", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get OAuth authorization server metadata: " + throwable.getMessage(),
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
                    log.error("Nokia NAC Metadata API error - Status: {}, Body: {}, Headers: {}",
                            statusCode, errorBody, response.headers().asHttpHeaders());

                    String errorMessage = String.format(
                            "Nokia NAC Metadata API error [%s]: %s",
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
                            String.format("Nokia NAC Metadata API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
