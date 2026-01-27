package com.service.connectivityservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility class to extract information from JWT token
 */
@Slf4j
@Component
public class JwtTokenUtil {

    /**
     * Get username from JWT token
     * @return Username or null if not available
     */
    public String getUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                // Try different claim names for username
                String username = jwt.getClaimAsString("username");
                if (username == null) {
                    username = jwt.getClaimAsString("sub"); // Subject is usually the username
                }
                if (username == null) {
                    username = jwt.getClaimAsString("preferred_username");
                }
                return username;
            }
        } catch (Exception e) {
            log.warn("Failed to extract username from JWT token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get email from JWT token
     * @return Email or null if not available
     */
    public String getEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                return jwt.getClaimAsString("email");
            }
        } catch (Exception e) {
            log.warn("Failed to extract email from JWT token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get full JWT token object
     * @return Jwt object or null if not available
     */
    public Jwt getJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                return jwt;
            }
        } catch (Exception e) {
            log.warn("Failed to extract JWT token: {}", e.getMessage());
        }
        return null;
    }
}
