package com.service.authservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to expose OpenID Connect Discovery endpoint
 * Provides /.well-known/openid-configuration endpoint
 */
@RestController
@RequestMapping("/.well-known")
public class OidcController {

    @Value("${spring.security.oauth2.authorization-server.issuer-uri:http://localhost:8085}")
    private String issuerUri;

    @GetMapping(value = "/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> openidConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("issuer", issuerUri);
        config.put("authorization_endpoint", issuerUri + "/oauth2/authorize");
        config.put("token_endpoint", issuerUri + "/oauth2/token");
        config.put("jwks_uri", issuerUri + "/.well-known/jwks.json");
        config.put("response_types_supported", new String[]{"code", "token", "id_token"});
        config.put("subject_types_supported", new String[]{"public"});
        config.put("id_token_signing_alg_values_supported", new String[]{"RS256"});
        config.put("scopes_supported", new String[]{"openid", "profile", "read", "write"});
        config.put("token_endpoint_auth_methods_supported", new String[]{"client_secret_basic", "client_secret_post"});
        config.put("grant_types_supported", new String[]{"authorization_code", "refresh_token", "client_credentials"});
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(config);
    }
}
