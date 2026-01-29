package com.service.devicemanagementservice.client;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNacSimSwapCheckClient {
    Mono<Map<String,Object>> retrieveSimSwap(com.service.shared.dto.request.DeviceDTO device);
}
