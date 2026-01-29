package com.service.devicemanagementservice.service.impl;

import com.service.devicemanagementservice.client.NokiaNacDeviceSwapClient;
import com.service.devicemanagementservice.service.DeviceSwapService;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CheckDeviceSwap;
import com.service.shared.dto.request.DeviceDTO;
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
public class DeviceSwapServiceImpl implements DeviceSwapService {

    private final NokiaNacDeviceSwapClient nokiaNacDeviceSwapClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse retrieveDeviceSwapDate(DeviceDTO device) {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceSwapClient.retrieveDeviceSwapDate(device);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve device swap date from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Device swap date retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving device swap date", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve device swap date: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse checkDeviceSwap(CheckDeviceSwap swap) {
        try {
            Mono<Map<String, Object>> resMono = nokiaNacDeviceSwapClient.CheckDeviceSwap(swap);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to check device swap from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Device swap check completed successfully", response);
        } catch (Exception e) {
            log.error("Error checking device swap", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to check device swap: " + e.getMessage()
            );
        }
    }
}
