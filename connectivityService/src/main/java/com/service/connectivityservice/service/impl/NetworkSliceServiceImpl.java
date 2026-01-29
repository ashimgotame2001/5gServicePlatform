package com.service.connectivityservice.service.impl;

import com.service.connectivityservice.client.NokiaNacNetworkSliceClient;
import com.service.connectivityservice.service.NetworkSliceService;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateNetworkSliceSubscriptionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkSliceServiceImpl implements NetworkSliceService {

    private final NokiaNacNetworkSliceClient networkSliceClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse createNetworkSliceSubscription(CreateNetworkSliceSubscriptionDTO request) {
        try {
            if (request == null) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Network slice subscription request cannot be null"
                );
            }

            log.debug("Creating network slice subscription for device: {}", 
                    request.getConfig() != null && 
                    request.getConfig().getSubscriptionDetail() != null &&
                    request.getConfig().getSubscriptionDetail().getDevice() != null ?
                    request.getConfig().getSubscriptionDetail().getDevice().getPhoneNumber() : "unknown");

            Mono<Map<String, Object>> resMono = networkSliceClient.createNetworkSliceSubscription(request);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create network slice subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Network slice subscription created successfully", response);
        } catch (Exception e) {
            log.error("Error creating network slice subscription", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create network slice subscription: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getAllNetworkSliceSubscriptions() {
        try {
            log.debug("Retrieving all network slice subscriptions");

            Mono<Map<String, Object>> resMono = networkSliceClient.getAllNetworkSliceSubscriptions();
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve network slice subscriptions from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Network slice subscriptions retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving all network slice subscriptions", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve network slice subscriptions: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getNetworkSliceSubscriptionById(String subscriptionId) {
        try {
            if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Subscription ID cannot be null or empty"
                );
            }

            log.debug("Retrieving network slice subscription by ID: {}", subscriptionId);

            Mono<Map<String, Object>> resMono = networkSliceClient.getNetworkSliceSubscriptionById(subscriptionId);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve network slice subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Network slice subscription retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving network slice subscription by ID: {}", subscriptionId, e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve network slice subscription: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse deleteNetworkSliceSubscription(String subscriptionId) {
        try {
            if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Subscription ID cannot be null or empty"
                );
            }

            log.debug("Deleting network slice subscription by ID: {}", subscriptionId);

            Mono<Map<String, Object>> resMono = networkSliceClient.deleteNetworkSliceSubscription(subscriptionId);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to delete network slice subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Network slice subscription deleted successfully", response);
        } catch (Exception e) {
            log.error("Error deleting network slice subscription: {}", subscriptionId, e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete network slice subscription: " + e.getMessage()
            );
        }
    }
}
