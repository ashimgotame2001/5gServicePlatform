package com.service.locationservice.client.impl;

import com.service.locationservice.client.NokiaNocLocationVerificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
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
public class NokiaNocLocationVerificationClientImpl implements NokiaNocLocationVerificationClient {

    private static final String DEVICE_STATUS_PATH = "https://location-verification.p-eu.rapidapi.com/";
    private static final String CONNECTIVITY_STATUS_PATH = DEVICE_STATUS_PATH + "v1/verify";
    private static final String CONNECTIVITY_STATUS_PATH_V2 = DEVICE_STATUS_PATH + "v2/verify";
    private static final String CONNECTIVITY_STATUS_PATH_v3 = DEVICE_STATUS_PATH + "v3/verify";
    private static final String host = "location-verification.p-eu.rapidapi.com";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);


    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;


    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNocLocationVerificationClientImpl(
            @Qualifier("nokiaWebClient") WebClient webClient,
            @Value("${nokia.nac.timeout:30000}") int timeoutMs,
            @Value("${nokia.nac.retry-attempts:3}") int retryAttempts, com.service.shared.util.ClientUtil clientUtil
    ) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.retrySpec = createRetrySpec(retryAttempts);
    }


    @Override
    public Mono<Map<String, Object>> verifyLocation(com.service.shared.dto.request.LocationVerificationDto request, String version) {
        String url = switch (version) {
            case "v2" -> CONNECTIVITY_STATUS_PATH_V2;
            case "v3" -> CONNECTIVITY_STATUS_PATH_v3;
            default -> CONNECTIVITY_STATUS_PATH;
        };
        return webClient.post()
                .uri(url)
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
                .doOnSuccess(result -> log.info("Retrieved device connectivity status successfully: {}", result))
                .doOnError(error -> log.error("Failed to get device connectivity status", error))
                .onErrorMap(throwable -> {
                    if (throwable instanceof com.service.shared.exception.GlobalException) {
                        return throwable;
                    }
                    return new com.service.shared.exception.GlobalException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get device connectivity status: " + throwable.getMessage(),
                            throwable);
                });
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
                            String.format("Nokia NAC API error [%s]: Unable to read error response", statusCode),
                            throwable
                    );
                    return Mono.<Throwable>error(exception);
                });
    }
}
