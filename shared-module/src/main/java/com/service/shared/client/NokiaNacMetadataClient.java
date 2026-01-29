package com.service.shared.client;

import com.service.shared.dto.NokiaNacMetadataDTO;
import reactor.core.publisher.Mono;

/**
 * Client interface for Nokia NAC Metadata endpoints
 * Handles OpenID configuration and OAuth authorization server metadata
 */
public interface NokiaNacMetadataClient {

    /**
     * Get OpenID configuration metadata
     * 
     * @return OpenID configuration metadata schema
     */
    Mono<NokiaNacMetadataDTO> getOpenIdConfiguration();

    /**
     * Get security.txt content
     * 
     * @return Security.txt content (string)
     */
    Mono<String> getSecurityTxt();

    /**
     * Get OAuth authorization server metadata
     * 
     * @return OAuth authorization server metadata schema
     */
    Mono<NokiaNacMetadataDTO> getOAuthAuthorizationServer();
}
