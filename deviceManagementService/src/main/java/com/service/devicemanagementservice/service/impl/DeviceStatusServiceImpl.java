package com.service.devicemanagementservice.service.impl;

import com.service.devicemanagementservice.client.NokiaNacDeviceStatusClient;
import com.service.devicemanagementservice.service.DeviceStatusService;
import com.service.shared.dto.CreateDeviceStatusSubscriptionDTO;
import com.service.shared.dto.DeviceConnectivityStatusDTO;
import com.service.shared.dto.GlobalResponse;
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
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final NokiaNacDeviceStatusClient nokiaNacDeviceStatusClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse getDeviceConnectivityStatus(DeviceConnectivityStatusDTO status) {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceStatusClient.getDeviceConnectivityStatus(status);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get device connectivity status from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Device connectivity status retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving device connectivity status", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get device connectivity status: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getDeviceRoamingStatus(DeviceConnectivityStatusDTO status) {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceStatusClient.getDeviceRoamingStatus(status);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get device roaming status from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Device roaming status retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving device roaming status", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get device roaming status: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getAllSubscriptions() {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceStatusClient.getAllSubscription();
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get all subscriptions from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Subscriptions retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving all subscriptions", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get all subscriptions: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse createDeviceStatusSubscription(CreateDeviceStatusSubscriptionDTO request) {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceStatusClient.createDeviceStatusSubscription(request);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create device status subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(201, "Device status subscription created successfully", response);
        } catch (Exception e) {
            log.error("Error creating device status subscription", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create device status subscription: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getSubscriptionById(String subscriptionId) {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceStatusClient.getSubscriptionById(subscriptionId);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get subscription from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Subscription retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving subscription by ID: {}", subscriptionId, e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get subscription: " + e.getMessage()
            );
        }
    }
}
