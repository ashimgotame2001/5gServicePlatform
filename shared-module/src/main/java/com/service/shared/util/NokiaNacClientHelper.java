package com.service.shared.util;

import com.service.shared.service.NokiaNacTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Helper utility for adding OAuth2 authorization to Nokia NAC API requests
 */
@Component
@RequiredArgsConstructor
public class NokiaNacClientHelper {

    private final NokiaNacTokenManager tokenManager;

    @Value("${nokia.nac.rapidapi-key}")
    private String rapidApiKey;

    @Value("${nokia.nac.rapidapi-host:network-as-code.nokia.rapidapi.com}")
    private String rapidApiHost;

    /**
     * Add required headers including OAuth2 Bearer token to a WebClient request
     */
    public WebClient.RequestHeadersSpec<?> addAuthHeaders(WebClient.RequestHeadersSpec<?> requestSpec) {
        String accessToken = tokenManager.getAccessToken();
        return requestSpec
                .header("X-RapidAPI-Key", rapidApiKey)
                .header("X-RapidAPI-Host", rapidApiHost)
                .header("Authorization", "Bearer " + accessToken);
    }

    /**
     * Get OAuth2 access token
     */
    public String getAccessToken() {
        return tokenManager.getAccessToken();
    }
}
