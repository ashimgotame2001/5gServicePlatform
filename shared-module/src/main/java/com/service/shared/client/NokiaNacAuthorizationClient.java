package com.service.shared.client;

import com.service.shared.dto.NokiaNacTokenRequestDTO;
import com.service.shared.dto.NokiaNacTokenResponseDTO;
import reactor.core.publisher.Mono;

/**
 * Client interface for Nokia NAC OAuth2 Authorization Server
 * Handles token requests and authorization flows
 */
public interface NokiaNacAuthorizationClient {

    /**
     * Request access token using client credentials grant
     * 
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @param scope Requested scope
     * @return Token response
     */
    Mono<NokiaNacTokenResponseDTO> requestClientCredentialsToken(String clientId, String clientSecret, String scope);

    /**
     * Request access token using authorization code grant
     * 
     * @param code Authorization code
     * @param redirectUri Redirect URI
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @return Token response
     */
    Mono<NokiaNacTokenResponseDTO> requestAuthorizationCodeToken(String code, String redirectUri, 
                                                                  String clientId, String clientSecret);

    /**
     * Refresh access token using refresh token grant
     * 
     * @param refreshToken Refresh token
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @return Token response
     */
    Mono<NokiaNacTokenResponseDTO> refreshToken(String refreshToken, String clientId, String clientSecret);

    /**
     * Request token using custom token request
     * 
     * @param tokenRequest Token request DTO
     * @return Token response
     */
    Mono<NokiaNacTokenResponseDTO> requestToken(NokiaNacTokenRequestDTO tokenRequest);
}
