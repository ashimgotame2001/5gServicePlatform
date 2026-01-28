package com.service.devicemanagementservice.client;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNacClient {

    Mono<Map<String, Object>> getDeviceConnectivityStatus(com.service.shared.dto.DeviceConnectivityStatusDTO status);
    Mono<Map<String, Object>> getDeviceRoamingStatus(com.service.shared.dto.DeviceConnectivityStatusDTO status);
    Mono<Map<String, Object>> getAllSubscription();
    Mono<Map<String, Object>> createDeviceStatusSubscription(com.service.shared.dto.CreateDeviceStatusSubscriptionDTO request);
    Mono<Map<String, Object>> getSubscriptionById(String SubscriptionId);

}
