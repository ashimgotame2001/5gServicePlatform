package com.service.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller to expose JWK Set endpoint for OAuth2 Resource Server validation
 * Returns the public keys used to verify JWT tokens
 */
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwkSetController {

    private final JWKSet jwkSet;

    @GetMapping(value = "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> jwks() {
        try {
            // Convert JWKSet to Map for JSON response
            Map<String, Object> jwksMap = jwkSet.toJSONObject();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jwksMap);
        } catch (Exception e) {
            // Fallback: return empty keys array
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("keys", new Object[0]));
        }
    }
}
