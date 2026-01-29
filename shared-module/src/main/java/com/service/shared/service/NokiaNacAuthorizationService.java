package com.service.shared.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NokiaNacTokenRequestDTO;

/**
 * Service interface for Nokia NAC OAuth2 Authorization
 * Handles token management for Nokia NAC API access
 */
public interface NokiaNacAuthorizationService {

    /**
     * Request access token using client credentials grant
     * 
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @param scope Requested scope
     * @return Token response
     */
    GlobalResponse requestClientCredentialsToken(String clientId, String clientSecret, String scope);

    /**
     * Request access token using authorization code grant
     * 
     * @param code Authorization code
     * @param redirectUri Redirect URI
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @return Token response
     */
    GlobalResponse requestAuthorizationCodeToken(String code, String redirectUri, 
                                                String clientId, String clientSecret);

    /**
     * Refresh access token
     * 
     * @param refreshToken Refresh token
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @return Token response
     */
    GlobalResponse refreshToken(String refreshToken, String clientId, String clientSecret);

    /**
     * Request token using custom token request
     * 
     * @param tokenRequest Token request DTO
     * @return Token response
     */
    GlobalResponse requestToken(NokiaNacTokenRequestDTO tokenRequest);

    /**
     * Get authorization URL for authorization code flow
     * 
     * @param clientId Client ID
     * @param redirectUri Redirect URI
     * @param scope Requested scope
     * @param state State parameter for CSRF protection
     * @return Authorization URL
     */
    GlobalResponse getAuthorizationUrl(String clientId, String redirectUri, String scope, String state);
}
