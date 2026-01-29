package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Nokia NAC OAuth2 Token Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NokiaNacTokenResponseDTO {

    /**
     * Access token
     */
    private String accessToken;

    /**
     * Token type (usually "Bearer")
     */
    private String tokenType;

    /**
     * Expires in (seconds)
     */
    private Integer expiresIn;

    /**
     * Refresh token
     */
    private String refreshToken;

    /**
     * Scope
     */
    private String scope;

    /**
     * ID token (for OpenID Connect)
     */
    private String idToken;
}
