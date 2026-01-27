package com.service.connectivityservice.client;

import com.service.connectivityservice.dto.request.CreateSessionRequestDTO;
import com.service.connectivityservice.dto.request.DeviceRequestDTO;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNacClient {

    Mono<Map<String, Object>> createSession(CreateSessionRequestDTO requestDTO);

    Mono<Map<String, Object>> getSessions();


    Mono<Map<String, Object>> retrieveSessions(DeviceRequestDTO request);

    Mono<Map<String, Object>> getSession(String sessionId);

    Mono<Map<String, Object>> postRequest(String endpoint, Object requestBody);
}
