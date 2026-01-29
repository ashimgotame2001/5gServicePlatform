package com.service.devicemanagementservice.client;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNacDeviceSwapClient {


    Mono<Map<String, Object>> retrieveDeviceSwapDate(com.service.shared.dto.request.DeviceDTO device);
    Mono<Map<String, Object>> CheckDeviceSwap(com.service.shared.dto.request.CheckDeviceSwap swap);


}
