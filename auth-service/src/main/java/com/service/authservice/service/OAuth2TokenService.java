package com.service.authservice.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for generating OAuth2 JWT tokens using RSA keys
 * This ensures tokens are signed with the same keys as the OAuth2 Authorization Server
 */
@Service
@RequiredArgsConstructor
public class OAuth2TokenService {

    private final JWKSet jwkSet;
    
    @Value("${jwt.expiration:86400000}") // 24 hours default
    private Long expiration;
    
    @Value("${spring.security.oauth2.authorization-server.issuer-uri:http://localhost:8085}")
    private String issuerUri;

    /**
     * Generate a JWT token signed with RSA keys (same as OAuth2 Authorization Server)
     */
    public String generateToken(String username, String email) {
        try {
            // Get the RSA key from the JWK Set
            RSAKey rsaKey = jwkSet.getKeys().stream()
                    .filter(key -> key instanceof RSAKey)
                    .map(key -> (RSAKey) key)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No RSA key found in JWK Set"));

            // Create JWT claims
            Instant now = Instant.now();
            Instant expiry = now.plusMillis(expiration);
            
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer(issuerUri)
                    .subject(username)
                    .audience(List.of("smart-5g-client"))
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("email", email)
                    .claim("username", username)
                    .build();

            // Create signed JWT
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID(rsaKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            
            // Sign with RSA private key
            RSASSASigner signer = new RSASSASigner(rsaKey.toRSAPrivateKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}
