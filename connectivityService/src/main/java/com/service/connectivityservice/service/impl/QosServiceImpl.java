package com.service.connectivityservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.connectivityservice.client.NokiaNacQosClient;
import com.service.shared.dto.request.CreateSessionRequestDTO;
import com.service.shared.dto.request.DeviceRequestDTO;
import com.service.connectivityservice.service.QosService;
import com.service.connectivityservice.util.JwtTokenUtil;
import com.service.shared.dto.GlobalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QosServiceImpl implements QosService {

    private final NokiaNacQosClient nokiaNacQosClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse getRetrieveSessions(DeviceRequestDTO request) {


        try {
            Mono<Map<String, Object>> resMono = nokiaNacQosClient.retrieveSessions(request);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve sessions from Nokia API"
                );
            }


            return GlobalResponse.successWithData(200, "Data fetched successfully", response);
        } catch (Exception e) {
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve sessions: " + e.getMessage()
            );
        }
    }


    @Override
    @Transactional
    public GlobalResponse createSession(CreateSessionRequestDTO requestDTO) {

        try {
            // Convert DTO to Map for API call
            Mono<Map<String, Object>> resMono = nokiaNacQosClient.createSession(requestDTO);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create session from Nokia API"
                );
            }

            // Save session to database

            return GlobalResponse.successWithData(201, "Session created successfully", response);
        } catch (Exception e) {
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create session: " + e.getMessage()
            );
        }
    }

    @Override
    public GlobalResponse getSession(String sessionId) {
        Mono<Map<String, Object>> res = nokiaNacQosClient.getSession(sessionId);
        Map<String, Object> response = res.block(BLOCK_TIMEOUT);
        if (response == null) {
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create session from Nokia API"
            );
        }

        return GlobalResponse.successWithData(200, "Session retrieve successfully", response);
    }

    @Override
    public GlobalResponse getSessions() {
        return null;
    }
}
