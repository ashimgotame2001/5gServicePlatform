package com.service.shared.client.impl;

import com.service.shared.client.NokiaNacAuthorizationClient;
import com.service.shared.dto.NokiaNacTokenRequestDTO;
import com.service.shared.dto.NokiaNacTokenResponseDTO;
import com.service.shared.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

/**
 * Implementation of Nokia NAC Authorization Client
 * Handles OAuth2 token requests to Nokia NAC authorization server
 */
@Slf4j
@Component
public class NokiaNacAuthorizationClientImpl implements NokiaNacAuthorizationClient {

    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    
    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;
    
    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;
    
    @Value("${nokia.nac.authorization-server-url:}")
    private String authorizationServerUrl;
    
    public NokiaNacAuthorizationClientImpl(
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
                        log.warn("Retrying Nokia NAC Authorization API call. Attempt: {}/{}",
                                retrySignal.totalRetries() + 1, retryAttempts))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    Throwable failure = retrySignal.failure();
                    log.error("Nokia NAC Authorization API retry exhausted after {} attempts", retryAttempts, failure);
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            String.format("Nokia NAC Authorization API retry exhausted after %d attempts: %s",
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
    
    /**
     * Get token endpoint URL from authorization server metadata
     * In production, this should be retrieved from the metadata endpoint
     */
    private String getTokenEndpoint() {
        // Default token endpoint - should be retrieved from metadata in production
        if (authorizationServerUrl != null && !authorizationServerUrl.isEmpty()) {
            // Try both /token and /oauth2/token paths
            String endpoint = authorizationServerUrl + "/token";
            log.debug("Using token endpoint: {}", endpoint);
            return endpoint;
        }
        // Fallback to a default endpoint structure
        String fallbackEndpoint = "https://authorization.p-eu.rapidapi.com/token";
        log.debug("Using fallback token endpoint: {}", fallbackEndpoint);
        return fallbackEndpoint;
    }
    
    @Override
    public Mono<NokiaNacTokenResponseDTO> requestClientCredentialsToken(String clientId, String clientSecret, String scope) {
        log.info("Requesting client credentials token for client: {} (scope: {})", clientId, scope);
        
        if (clientId == null || clientId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("clientId cannot be null or empty"));
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            return Mono.error(new IllegalArgumentException("clientSecret cannot be null or empty"));
        }
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        if (scope != null && !scope.isEmpty()) {
            formData.add("scope", scope);
        }
        
        return requestTokenInternal(formData);
    }
    
    @Override
    public Mono<NokiaNacTokenResponseDTO> requestAuthorizationCodeToken(String code, String redirectUri,
                                                                          String clientId, String clientSecret) {
        log.debug("Requesting authorization code token for client: {}", clientId);
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        
        return requestTokenInternal(formData);
    }
    
    @Override
    public Mono<NokiaNacTokenResponseDTO> refreshToken(String refreshToken, String clientId, String clientSecret) {
        log.debug("Refreshing token for client: {}", clientId);
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        
        return requestTokenInternal(formData);
    }
    
    @Override
    public Mono<NokiaNacTokenResponseDTO> requestToken(NokiaNacTokenRequestDTO tokenRequest) {
        log.debug("Requesting token with grant type: {}", tokenRequest.getGrantType());
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", tokenRequest.getGrantType());
        
        if (tokenRequest.getCode() != null) {
            formData.add("code", tokenRequest.getCode());
        }
        if (tokenRequest.getRedirectUri() != null) {
            formData.add("redirect_uri", tokenRequest.getRedirectUri());
        }
        if (tokenRequest.getClientId() != null) {
            formData.add("client_id", tokenRequest.getClientId());
        }
        if (tokenRequest.getClientSecret() != null) {
            formData.add("client_secret", tokenRequest.getClientSecret());
        }
        if (tokenRequest.getRefreshToken() != null) {
            formData.add("refresh_token", tokenRequest.getRefreshToken());
        }
        if (tokenRequest.getScope() != null) {
            formData.add("scope", tokenRequest.getScope());
        }
        
        return requestTokenInternal(formData);
    }
    
    private Mono<NokiaNacTokenResponseDTO> requestTokenInternal(MultiValueMap<String, String> formData) {
        String tokenEndpoint = getTokenEndpoint();
        
        log.debug("Making token request to: {} with grant_type: {}", tokenEndpoint, formData.getFirst("grant_type"));
        
        return webClient.post()
                .uri(URI.create(tokenEndpoint))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", "authorization.nokia.rapidapi.com")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(NokiaNacTokenResponseDTO.class)
                .timeout(timeout)
                .retryWhen(retrySpec)
                .doOnSuccess(result -> log.info("Token request successful, expires in {} seconds", 
                        result.getExpiresIn() != null ? result.getExpiresIn() : "unknown"))
                .doOnError(error -> log.error("Failed to request token from endpoint: {}", tokenEndpoint, error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof GlobalException) {
                        return throwable;
                    }
                    return new GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to request token: " + throwable.getMessage(),
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
                    log.error("Nokia NAC Authorization API error - Status: {}, Body: {}, Headers: {}",
                            statusCode, errorBody, response.headers().asHttpHeaders());
                    
                    String errorMessage = String.format(
                            "Nokia NAC Authorization API error [%s]: %s",
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
                            String.format("Nokia NAC Authorization API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
