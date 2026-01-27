package com.service.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;


public abstract class BaseSecurityConfig {

    /**
     * Create a SecurityFilterChain with common OAuth2 settings
     * @param http HttpSecurity instance
     * @param jwtDecoder JWT decoder bean
     * @param permitAllPaths Additional paths to permit without authentication (e.g., "/connectivity/**")
     * @return Configured SecurityFilterChain
     */
    protected SecurityFilterChain createSecurityFilterChain(
            HttpSecurity http, 
            JwtDecoder jwtDecoder,
            String... permitAllPaths) throws Exception {
        
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/**", "/health").permitAll();
                    if (permitAllPaths != null && permitAllPaths.length > 0) {
                        auth.requestMatchers(permitAllPaths).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder))
                );

        return http.build();
    }
}
