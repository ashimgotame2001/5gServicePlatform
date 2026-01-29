package com.service.shared.controller;

import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NokiaNacTokenRequestDTO;
import com.service.shared.service.NokiaNacAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Nokia NAC OAuth2 Authorization endpoints
 * Handles token requests and authorization flows
 */
@RestController
@RequestMapping("/nokia-nac/authorization")
@RequiredArgsConstructor
public class NokiaNacAuthorizationController {

    private final NokiaNacAuthorizationService authorizationService;

    /**
     * Request access token using client credentials grant
     */
    @PostMapping("/token/client-credentials")
    @MethodCode(value = "NA001", description = "Request token using client credentials grant")
    public ResponseEntity<GlobalResponse> requestClientCredentialsToken(
            @RequestParam String clientId,
            @RequestParam String clientSecret,
            @RequestParam(required = false) String scope) {
        return ResponseEntity.ok(authorizationService.requestClientCredentialsToken(clientId, clientSecret, scope));
    }

    /**
     * Request access token using authorization code grant
     */
    @PostMapping("/token/authorization-code")
    @MethodCode(value = "NA002", description = "Request token using authorization code grant")
    public ResponseEntity<GlobalResponse> requestAuthorizationCodeToken(
            @RequestParam String code,
            @RequestParam String redirectUri,
            @RequestParam String clientId,
            @RequestParam String clientSecret) {
        return ResponseEntity.ok(authorizationService.requestAuthorizationCodeToken(code, redirectUri, clientId, clientSecret));
    }

    /**
     * Refresh access token
     */
    @PostMapping("/token/refresh")
    @MethodCode(value = "NA003", description = "Refresh access token")
    public ResponseEntity<GlobalResponse> refreshToken(
            @RequestParam String refreshToken,
            @RequestParam String clientId,
            @RequestParam String clientSecret) {
        return ResponseEntity.ok(authorizationService.refreshToken(refreshToken, clientId, clientSecret));
    }

    /**
     * Request token using custom token request
     */
    @PostMapping("/token")
    @MethodCode(value = "NA004", description = "Request token with custom grant type")
    public ResponseEntity<GlobalResponse> requestToken(@RequestBody NokiaNacTokenRequestDTO tokenRequest) {
        return ResponseEntity.ok(authorizationService.requestToken(tokenRequest));
    }

    /**
     * Get authorization URL for authorization code flow
     */
    @GetMapping("/authorize")
    @MethodCode(value = "NA005", description = "Get authorization URL")
    public ResponseEntity<GlobalResponse> getAuthorizationUrl(
            @RequestParam String clientId,
            @RequestParam String redirectUri,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String state) {
        return ResponseEntity.ok(authorizationService.getAuthorizationUrl(clientId, redirectUri, scope, state));
    }
}
