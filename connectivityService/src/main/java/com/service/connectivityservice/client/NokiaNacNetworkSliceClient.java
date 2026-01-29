package com.service.connectivityservice.client;

import com.service.shared.dto.request.CreateNetworkSliceSubscriptionDTO;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNacNetworkSliceClient {

    Mono<Map<String, Object>> createNetworkSliceSubscription(CreateNetworkSliceSubscriptionDTO request);

    Mono<Map<String, Object>> getAllNetworkSliceSubscriptions();

    Mono<Map<String, Object>> getNetworkSliceSubscriptionById(String subscriptionId);

    Mono<Map<String, Object>> deleteNetworkSliceSubscription(String subscriptionId);
}
