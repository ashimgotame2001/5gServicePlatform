package com.service.devicemanagementservice.service;

import com.service.shared.dto.CreateDeviceStatusSubscriptionDTO;
import com.service.shared.dto.DeviceConnectivityStatusDTO;
import com.service.shared.dto.GlobalResponse;

public interface DeviceStatusService {

    GlobalResponse getDeviceConnectivityStatus(DeviceConnectivityStatusDTO status);

    GlobalResponse getDeviceRoamingStatus(DeviceConnectivityStatusDTO status);

    GlobalResponse getAllSubscriptions();

    GlobalResponse createDeviceStatusSubscription(CreateDeviceStatusSubscriptionDTO request);

    GlobalResponse getSubscriptionById(String subscriptionId);
}
