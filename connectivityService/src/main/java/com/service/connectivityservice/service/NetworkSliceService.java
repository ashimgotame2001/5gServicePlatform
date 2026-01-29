package com.service.connectivityservice.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateNetworkSliceSubscriptionDTO;

public interface NetworkSliceService {

    GlobalResponse createNetworkSliceSubscription(CreateNetworkSliceSubscriptionDTO request);

    GlobalResponse getAllNetworkSliceSubscriptions();

    GlobalResponse getNetworkSliceSubscriptionById(String subscriptionId);

    GlobalResponse deleteNetworkSliceSubscription(String subscriptionId);
}
