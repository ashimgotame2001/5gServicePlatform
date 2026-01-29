package com.service.shared.service.impl;

import com.service.shared.client.NokiaNacMetadataClient;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NokiaNacMetadataDTO;
import com.service.shared.service.NokiaNacMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NokiaNacMetadataServiceImpl implements NokiaNacMetadataService {

    private final NokiaNacMetadataClient nokiaNacMetadataClient;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse getOpenIdConfiguration() {
        try {
            Mono<NokiaNacMetadataDTO> resMono = nokiaNacMetadataClient.getOpenIdConfiguration();
            NokiaNacMetadataDTO response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get OpenID configuration from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "OpenID configuration retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving OpenID configuration", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get OpenID configuration: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getSecurityTxt() {
        try {
            Mono<String> resMono = nokiaNacMetadataClient.getSecurityTxt();
            String response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get security.txt from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "Security.txt retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving security.txt", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get security.txt: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getOAuthAuthorizationServer() {
        try {
            Mono<NokiaNacMetadataDTO> resMono = nokiaNacMetadataClient.getOAuthAuthorizationServer();
            NokiaNacMetadataDTO response = resMono.block(BLOCK_TIMEOUT);

            if (response == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get OAuth authorization server metadata from Nokia API"
                );
            }

            return GlobalResponse.successWithData(200, "OAuth authorization server metadata retrieved successfully", response);
        } catch (Exception e) {
            log.error("Error retrieving OAuth authorization server metadata", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get OAuth authorization server metadata: " + e.getMessage()
            );
        }
    }
}
