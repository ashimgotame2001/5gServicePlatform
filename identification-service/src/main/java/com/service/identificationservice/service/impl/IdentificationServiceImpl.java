package com.service.identificationservice.service.impl;

import com.service.identificationservice.client.NokiaNocPhoneNumberVerificationClient;
import com.service.identificationservice.service.IdentificationService;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.PhoneVerificationRequest;
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
public class IdentificationServiceImpl implements IdentificationService {

    private final NokiaNocPhoneNumberVerificationClient phoneNumberVerificationClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse verifyPhoneNumber(PhoneVerificationRequest request) {
        try {
            if (request == null) {
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        "Phone verification request cannot be null"
                );
            }

            log.debug("Verifying phone number: {}", 
                    request.getPhoneNumber() != null ? request.getPhoneNumber() : "unknown");

            Mono<Map<String, Object>> resMono = phoneNumberVerificationClient.verifyPhoneNumber(request);
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to verify phone number from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Phone number verified successfully", response);
        } catch (Exception e) {
            log.error("Error verifying phone number", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to verify phone number: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse sharePhoneNumber() {
        try {
            log.debug("Retrieving device phone number");

            Mono<Map<String, Object>> resMono = phoneNumberVerificationClient.sharePhoneNumber();
            Map<String, Object> response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to retrieve phone number from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Phone number retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving phone number", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve phone number: " + e.getMessage()
            );
        }
    }
}
