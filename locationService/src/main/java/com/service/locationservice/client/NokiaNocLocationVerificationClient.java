package com.service.locationservice.client;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNocLocationVerificationClient {

    Mono<Map<String,Object>> verifyLocation(com.service.shared.dto.request.LocationVerificationDto request,String version);
}
