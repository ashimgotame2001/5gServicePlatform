package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Nokia NAC OAuth2 Token Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NokiaNacTokenRequestDTO {

    /**
     * Grant type: authorization_code, client_credentials, refresh_token
     */
    private String grantType;

    /**
     * Authorization code (for authorization_code grant)
     */
    private String code;

    /**
     * Redirect URI (for authorization_code grant)
     */
    private String redirectUri;

    /**
     * Client ID
     */
    private String clientId;

    /**
     * Client secret
     */
    private String clientSecret;

    /**
     * Refresh token (for refresh_token grant)
     */
    private String refreshToken;

    /**
     * Scope
     */
    private String scope;
}
