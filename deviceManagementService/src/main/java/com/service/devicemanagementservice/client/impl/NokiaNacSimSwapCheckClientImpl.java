package com.service.devicemanagementservice.client.impl;

import com.service.devicemanagementservice.client.NokiaNacSimSwapCheckClient;
import com.service.shared.dto.request.DeviceDTO;
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
public class NokiaNacSimSwapCheckClientImpl implements NokiaNacSimSwapCheckClient {

    private static final String DEVICE_STATUS_PATH = "https://network-as-code.p-eu.rapidapi.com/";
    private static final String host = "network-as-code.p-eu.rapidapi.com";
    private static final String CONNECTIVITY_STATUS_PATH = DEVICE_STATUS_PATH + "passthrough/camara/v1/sim-swap/sim-swap/v0/retrieve-date";
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final Retry retrySpec;
    private final Duration timeout;

    @Value("${nokia.nac.rapidapi-key}")
    private String apiKey;

    public NokiaNacSimSwapCheckClientImpl(
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

    @Override
    public Mono<Map<String, Object>> retrieveSimSwap(DeviceDTO device) {
        if (device == null) {
            return Mono.error(new com.service.shared.exception.GlobalException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Device connectivity status request cannot be null"
            ));
        }

        log.debug("Fetching device connectivity status for device: {}", device.getPhoneNumber());

        return webClient.post()
                .uri(CONNECTIVITY_STATUS_PATH)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .bodyValue(device)
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
}
