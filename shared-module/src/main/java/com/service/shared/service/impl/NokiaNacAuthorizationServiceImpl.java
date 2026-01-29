package com.service.shared.service.impl;

import com.service.shared.client.NokiaNacAuthorizationClient;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NokiaNacTokenRequestDTO;
import com.service.shared.dto.NokiaNacTokenResponseDTO;
import com.service.shared.service.NokiaNacAuthorizationService;
import com.service.shared.service.NokiaNacMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Nokia NAC Authorization Service
 * Handles OAuth2 token management for Nokia NAC API access
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NokiaNacAuthorizationServiceImpl implements NokiaNacAuthorizationService {

    private final NokiaNacAuthorizationClient authorizationClient;
    private final NokiaNacMetadataService metadataService;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Transactional
    public GlobalResponse requestClientCredentialsToken(String clientId, String clientSecret, String scope) {
        try {
            log.info("Requesting client credentials token for client: {} (client_id length: {}, scope: {})", 
                    clientId != null ? (clientId.length() > 8 ? clientId.substring(0, 8) + "..." : clientId) : "null",
                    clientId != null ? clientId.length() : 0, scope);
            
            if (clientId == null || clientId.trim().isEmpty()) {
                String errorMsg = "client_id is null or empty";
                log.error(errorMsg);
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        errorMsg
                );
            }
            
            if (clientSecret == null || clientSecret.trim().isEmpty()) {
                String errorMsg = "client_secret is null or empty";
                log.error(errorMsg);
                return GlobalResponse.failure(
                        HttpStatus.BAD_REQUEST.value(),
                        errorMsg
                );
            }
            
            Mono<NokiaNacTokenResponseDTO> tokenMono = authorizationClient
                    .requestClientCredentialsToken(clientId, clientSecret, scope);
            
            NokiaNacTokenResponseDTO tokenResponse = tokenMono.block(BLOCK_TIMEOUT);
            
            if (tokenResponse == null) {
                log.error("Token response is null from authorization server");
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get token from Nokia NAC authorization server: response was null"
                );
            }
            
            log.info("Successfully retrieved token (expires_in: {})", 
                    tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn() : "unknown");
            return GlobalResponse.successWithData(200, "Token retrieved successfully", tokenResponse);
        } catch (Exception e) {
            String errorMsg = "Failed to request token: " + e.getMessage();
            log.error("Error requesting client credentials token. Exception type: {}, Message: {}", 
                    e.getClass().getSimpleName(), errorMsg, e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    errorMsg
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse requestAuthorizationCodeToken(String code, String redirectUri,
                                                       String clientId, String clientSecret) {
        try {
            log.info("Requesting authorization code token for client: {}", clientId);
            
            Mono<NokiaNacTokenResponseDTO> tokenMono = authorizationClient
                    .requestAuthorizationCodeToken(code, redirectUri, clientId, clientSecret);
            
            NokiaNacTokenResponseDTO tokenResponse = tokenMono.block(BLOCK_TIMEOUT);
            
            if (tokenResponse == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get token from Nokia NAC authorization server"
                );
            }
            
            return GlobalResponse.successWithData(200, "Token retrieved successfully", tokenResponse);
        } catch (Exception e) {
            log.error("Error requesting authorization code token", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to request token: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse refreshToken(String refreshToken, String clientId, String clientSecret) {
        try {
            log.info("Refreshing token for client: {}", clientId);
            
            Mono<NokiaNacTokenResponseDTO> tokenMono = authorizationClient
                    .refreshToken(refreshToken, clientId, clientSecret);
            
            NokiaNacTokenResponseDTO tokenResponse = tokenMono.block(BLOCK_TIMEOUT);
            
            if (tokenResponse == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to refresh token from Nokia NAC authorization server"
                );
            }
            
            return GlobalResponse.successWithData(200, "Token refreshed successfully", tokenResponse);
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to refresh token: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse requestToken(NokiaNacTokenRequestDTO tokenRequest) {
        try {
            log.info("Requesting token with grant type: {}", tokenRequest.getGrantType());
            
            Mono<NokiaNacTokenResponseDTO> tokenMono = authorizationClient.requestToken(tokenRequest);
            
            NokiaNacTokenResponseDTO tokenResponse = tokenMono.block(BLOCK_TIMEOUT);
            
            if (tokenResponse == null) {
                return GlobalResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get token from Nokia NAC authorization server"
                );
            }
            
            return GlobalResponse.successWithData(200, "Token retrieved successfully", tokenResponse);
        } catch (Exception e) {
            log.error("Error requesting token", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to request token: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public GlobalResponse getAuthorizationUrl(String clientId, String redirectUri, String scope, String state) {
        try {
            log.info("Getting authorization URL for client: {}", clientId);
            
            // Get authorization endpoint from metadata
            GlobalResponse metadataResponse = metadataService.getOAuthAuthorizationServer();
            
            String authorizationEndpoint = "https://authorization.p-eu.rapidapi.com/oauth2/authorize";
            
            if (metadataResponse != null && metadataResponse.getData() != null) {
                // Extract authorization endpoint from metadata if available
                // For now, use default endpoint
            }
            
            // Build authorization URL
            StringBuilder urlBuilder = new StringBuilder(authorizationEndpoint);
            urlBuilder.append("?response_type=code");
            urlBuilder.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            urlBuilder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            
            if (scope != null && !scope.isEmpty()) {
                urlBuilder.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
            }
            
            if (state != null && !state.isEmpty()) {
                urlBuilder.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("authorizationUrl", urlBuilder.toString());
            result.put("clientId", clientId);
            result.put("redirectUri", redirectUri);
            result.put("scope", scope);
            result.put("state", state);
            
            return GlobalResponse.successWithData(200, "Authorization URL generated successfully", result);
        } catch (Exception e) {
            log.error("Error generating authorization URL", e);
            return GlobalResponse.failure(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to generate authorization URL: " + e.getMessage()
            );
        }
    }
}
