package com.service.devicemanagementservice.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CheckDeviceSwap;
import com.service.shared.dto.request.DeviceDTO;

public interface DeviceSwapService {

    GlobalResponse retrieveDeviceSwapDate(DeviceDTO device);

    GlobalResponse checkDeviceSwap(CheckDeviceSwap swap);
}
