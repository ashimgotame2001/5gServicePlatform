package com.service.connectivityservice.client;

import com.service.shared.dto.request.CreateSessionRequestDTO;
import com.service.shared.dto.request.DeviceRequestDTO;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNacQosClient {

    Mono<Map<String, Object>> createSession(CreateSessionRequestDTO requestDTO);

    Mono<Map<String, Object>> getSessions();


    Mono<Map<String, Object>> retrieveSessions(DeviceRequestDTO request);

    Mono<Map<String, Object>> getSession(String sessionId);

    Mono<Map<String, Object>> postRequest(String endpoint, Object requestBody);
}
