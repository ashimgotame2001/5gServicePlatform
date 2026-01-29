package com.service.shared.service;

import com.service.shared.client.NokiaNacClientCredentialsClient;
import com.service.shared.dto.NokiaNacClientCredentialsDTO;
import com.service.shared.dto.NokiaNacTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token Manager for Nokia NAC OAuth2 tokens
 * Manages token lifecycle: acquisition, caching, and refresh
 * Dynamically retrieves client credentials from Nokia NAC authorization server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NokiaNacTokenManager {

    private final NokiaNacAuthorizationService authorizationService;
    private final NokiaNacClientCredentialsClient clientCredentialsClient;

    @Value("${nokia.nac.scope:read write}")
    private String scope;

    // Client credentials cache (retrieved dynamically)
    private volatile NokiaNacClientCredentialsDTO cachedCredentials;
    private final ReentrantLock credentialsLock = new ReentrantLock();

    // Token cache
    private volatile NokiaNacTokenResponseDTO cachedToken;
    private volatile LocalDateTime tokenExpiresAt;
    private final ReentrantLock tokenLock = new ReentrantLock();

    // Buffer time before expiration (5 minutes)
    private static final int REFRESH_BUFFER_SECONDS = 300;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Get client credentials (client_id and client_secret)
     * Retrieves from cache if available, otherwise fetches from Nokia NAC authorization server
     */
    private NokiaNacClientCredentialsDTO getClientCredentials() {
        credentialsLock.lock();
        try {
            // Return cached credentials if available
            if (cachedCredentials != null && 
                cachedCredentials.getClientId() != null && 
                !cachedCredentials.getClientId().isEmpty() &&
                cachedCredentials.getClientSecret() != null &&
                !cachedCredentials.getClientSecret().isEmpty()) {
                log.debug("Using cached client credentials");
                return cachedCredentials;
            }

            // Fetch new credentials
            log.info("Fetching client credentials from Nokia NAC authorization server");
            try {
                Mono<NokiaNacClientCredentialsDTO> credentialsMono = clientCredentialsClient.getClientCredentials();
                NokiaNacClientCredentialsDTO credentials = credentialsMono.block(BLOCK_TIMEOUT);

                if (credentials == null) {
                    log.error("Client credentials response is null");
                    throw new RuntimeException("Failed to retrieve client credentials: response was null");
                }

                log.debug("Received client credentials - client_id present: {}, client_secret present: {}", 
                        credentials.getClientId() != null, credentials.getClientSecret() != null);

                if (credentials.getClientId() == null || credentials.getClientId().trim().isEmpty()) {
                    log.error("Client credentials response has null or empty client_id");
                    throw new RuntimeException("Failed to retrieve client credentials: client_id is null or empty. Response: " + credentials);
                }

                if (credentials.getClientSecret() == null || credentials.getClientSecret().trim().isEmpty()) {
                    log.error("Client credentials response has null or empty client_secret");
                    throw new RuntimeException("Failed to retrieve client credentials: client_secret is null or empty. Response: " + credentials);
                }

                // Cache the credentials
                cachedCredentials = credentials;
                log.info("Successfully retrieved and cached client credentials (client_id length: {}, client_secret length: {})", 
                        credentials.getClientId().length(), credentials.getClientSecret().length());
                return credentials;
            } catch (Exception e) {
                log.error("Exception while fetching client credentials: {}", e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Error retrieving client credentials", e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Failed to retrieve client credentials: " + e.getMessage(), e);
        } finally {
            credentialsLock.unlock();
        }
    }

    /**
     * Get a valid access token
     * Returns cached token if valid, otherwise fetches a new one
     * Dynamically retrieves client credentials if needed
     */
    public String getAccessToken() {
        tokenLock.lock();
        try {
            // Check if we have a valid cached token
            if (cachedToken != null && tokenExpiresAt != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime refreshTime = tokenExpiresAt.minusSeconds(REFRESH_BUFFER_SECONDS);
                
                if (now.isBefore(refreshTime)) {
                    log.debug("Using cached access token");
                    return cachedToken.getAccessToken();
                } else {
                    log.info("Cached token expired or near expiration, refreshing...");
                }
            }

            // Get client credentials (from cache or fetch dynamically)
            NokiaNacClientCredentialsDTO credentials = getClientCredentials();
            String clientId = credentials.getClientId();
            String clientSecret = credentials.getClientSecret();

            log.debug("Using client_id: {} (length: {}), scope: {}", 
                    clientId != null ? clientId.substring(0, Math.min(8, clientId.length())) + "..." : "null",
                    clientId != null ? clientId.length() : 0, scope);

            // Fetch new token
            log.info("Fetching new OAuth2 token from Nokia NAC authorization server");
            try {
                var response = authorizationService.requestClientCredentialsToken(
                        clientId, clientSecret, scope);

                if (response.getCode() == 200 && response.getData() != null) {
                    cachedToken = (NokiaNacTokenResponseDTO) response.getData();
                    
                    // Calculate expiration time
                    int expiresIn = cachedToken.getExpiresIn() != null ? cachedToken.getExpiresIn() : 3600;
                    tokenExpiresAt = LocalDateTime.now().plusSeconds(expiresIn);
                    
                    log.info("Successfully obtained new access token, expires in {} seconds", expiresIn);
                    return cachedToken.getAccessToken();
                } else {
                    String errorMsg = String.format("Failed to obtain access token. Code: %d, Message: %s", 
                            response.getCode(), response.getMessage());
                    log.error("Token request failed: {}", errorMsg);
                    throw new RuntimeException("Failed to obtain Nokia NAC access token: " + errorMsg);
                }
            } catch (Exception e) {
                log.error("Error obtaining access token. Exception type: {}, Message: {}", 
                        e.getClass().getSimpleName(), e.getMessage(), e);
                if (e instanceof RuntimeException) {
                    throw e;
                }
                throw new RuntimeException("Failed to obtain Nokia NAC access token: " + e.getMessage(), e);
            }
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Force refresh the token
     */
    public void refreshToken() {
        tokenLock.lock();
        try {
            cachedToken = null;
            tokenExpiresAt = null;
            getAccessToken(); // This will fetch a new token
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Clear cached token (useful for testing or credential rotation)
     */
    public void clearToken() {
        tokenLock.lock();
        try {
            cachedToken = null;
            tokenExpiresAt = null;
            log.info("Cleared cached token");
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Clear cached client credentials (useful for credential rotation)
     */
    public void clearClientCredentials() {
        credentialsLock.lock();
        try {
            cachedCredentials = null;
            log.info("Cleared cached client credentials");
        } finally {
            credentialsLock.unlock();
        }
    }

    /**
     * Clear both token and client credentials cache
     */
    public void clearAll() {
        clearToken();
        clearClientCredentials();
    }
}
