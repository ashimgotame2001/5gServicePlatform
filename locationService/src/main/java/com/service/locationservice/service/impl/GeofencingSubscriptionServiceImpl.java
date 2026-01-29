package com.service.locationservice.service.impl;

import com.service.locationservice.client.NokiaNacGeofencingSubscriptionClient;
import com.service.locationservice.service.GeofencingSubscriptionService;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateGeofencingSubscriptionDTO;
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
public class GeofencingSubscriptionServiceImpl implements GeofencingSubscriptionService {

    private final NokiaNacGeofencingSubscriptionClient geofencingSubscriptionClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse createGeofencingSubscription(CreateGeofencingSubscriptionDTO request) {
        try {
            if (request == null) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Geofencing subscription request cannot be null"
                );
            }

            log.debug("Creating geofencing subscription for device: {}", 
                    request.getConfig() != null && 
                    request.getConfig().getSubscriptionDetail() != null &&
                    request.getConfig().getSubscriptionDetail().getDevice() != null ?
                    request.getConfig().getSubscriptionDetail().getDevice().getPhoneNumber() : "unknown");

            Mono<Map<String, Object>> resMono = geofencingSubscriptionClient.createGeofencingSubscription(request);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create geofencing subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Geofencing subscription created successfully", response);
        } catch (Exception e) {
            log.error("Error creating geofencing subscription", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create geofencing subscription: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getAllGeofencingSubscriptions() {
        try {
            log.debug("Retrieving all geofencing subscriptions");

            Mono<Map<String, Object>> resMono = geofencingSubscriptionClient.getAllGeofencingSubscriptions();
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve geofencing subscriptions from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Geofencing subscriptions retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving all geofencing subscriptions", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve geofencing subscriptions: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getGeofencingSubscriptionById(String subscriptionId) {
        try {
            if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Subscription ID cannot be null or empty"
                );
            }

            log.debug("Retrieving geofencing subscription by ID: {}", subscriptionId);

            Mono<Map<String, Object>> resMono = geofencingSubscriptionClient.getGeofencingSubscriptionById(subscriptionId);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve geofencing subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Geofencing subscription retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving geofencing subscription by ID: {}", subscriptionId, e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve geofencing subscription: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse deleteGeofencingSubscription(String subscriptionId) {
        try {
            if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Subscription ID cannot be null or empty"
                );
            }

            log.debug("Deleting geofencing subscription by ID: {}", subscriptionId);

            Mono<Map<String, Object>> resMono = geofencingSubscriptionClient.deleteGeofencingSubscription(subscriptionId);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to delete geofencing subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Geofencing subscription deleted successfully", response);
        } catch (Exception e) {
            log.error("Error deleting geofencing subscription: {}", subscriptionId, e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete geofencing subscription: " + e.getMessage()
            );
        }
    }
}
