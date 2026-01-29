package com.service.locationservice.client;

import com.service.shared.dto.request.CreateGeofencingSubscriptionDTO;
import reactor.core.publisher.Mono;

import java.util.Map;


public interface NokiaNacGeofencingSubscriptionClient {

    Mono<Map<String, Object>> createGeofencingSubscription(CreateGeofencingSubscriptionDTO request);

    Mono<Map<String, Object>> getAllGeofencingSubscriptions();

    Mono<Map<String, Object>> getGeofencingSubscriptionById(String subscriptionId);

    Mono<Map<String, Object>> deleteGeofencingSubscription(String subscriptionId);
}
