package com.service.locationservice.client;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNocLocationRetrievalClient {

    Mono<Map<String,Object>> retriveLocation(com.service.shared.dto.request.LocationRetrievalDTO request);
}
