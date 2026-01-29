package com.service.locationservice.service.impl;

import com.service.locationservice.client.NokiaNocLocationRetrievalClient;
import com.service.locationservice.client.NokiaNocLocationVerificationClient;
import com.service.locationservice.service.LocationService;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.LocationRetrievalDTO;
import com.service.shared.dto.request.LocationVerificationDto;
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
public class LocationServiceImpl implements LocationService {

    private final NokiaNocLocationVerificationClient locationVerificationClient;
    private final NokiaNocLocationRetrievalClient locationRetrievalClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse verifyLocation(LocationVerificationDto request, String version) {
        try {
            if (request == null) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Location verification request cannot be null"
                );
            }

            if (version == null || version.trim().isEmpty()) {
                version = "v1"; // Default version
            }

            log.debug("Verifying location for device: {} with version: {}", 
                    request.getDevice() != null ? request.getDevice().toString() : "unknown", version);

            Mono<Map<String, Object>> resMono = locationVerificationClient.verifyLocation(request, version);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to verify location from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Location verified successfully", response);
        } catch (Exception e) {
            log.error("Error verifying location", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to verify location: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse retrieveLocation(LocationRetrievalDTO request) {
        try {
            if (request == null) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Location retrieval request cannot be null"
                );
            }

            log.debug("Retrieving location for device: {}", 
                    request.getDevice() != null ? request.getDevice().toString() : "unknown");

            Mono<Map<String, Object>> resMono = locationRetrievalClient.retriveLocation(request);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve location from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Location retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving location", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve location: " + e.getMessage()
            );
        }
    }
}
