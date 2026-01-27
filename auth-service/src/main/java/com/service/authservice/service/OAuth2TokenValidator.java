package com.service.authservice.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

/**
 * Service for validating OAuth2 JWT tokens signed with RSA keys
 */
@Service
@RequiredArgsConstructor
public class OAuth2TokenValidator {

    private final JWKSet jwkSet;

    /**
     * Validate and extract claims from a JWT token
     */
    public JWTClaimsSet validateAndExtractClaims(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        
        // Get the RSA key from JWK Set
        RSAKey rsaKey = jwkSet.getKeys().stream()
                .filter(key -> key instanceof RSAKey)
                .map(key -> (RSAKey) key)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No RSA key found in JWK Set"));

        // Verify signature
        RSASSAVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
        if (!signedJWT.verify(verifier)) {
            throw new JOSEException("Invalid token signature");
        }

        // Get claims
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        
        // Check expiration
        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime != null && expirationTime.before(new Date())) {
            throw new JOSEException("Token has expired");
        }

        return claimsSet;
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        try {
            JWTClaimsSet claimsSet = validateAndExtractClaims(token);
            return claimsSet.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract username from token", e);
        }
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        try {
            JWTClaimsSet claimsSet = validateAndExtractClaims(token);
            return claimsSet.getStringClaim("email");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract email from token", e);
        }
    }

    /**
     * Extract expiration from token
     */
    public Date extractExpiration(String token) {
        try {
            JWTClaimsSet claimsSet = validateAndExtractClaims(token);
            return claimsSet.getExpirationTime();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract expiration from token", e);
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            JWTClaimsSet claimsSet = validateAndExtractClaims(token);
            Date expirationTime = claimsSet.getExpirationTime();
            return expirationTime != null && expirationTime.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
