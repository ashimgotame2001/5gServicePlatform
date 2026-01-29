package com.service.shared.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NokiaNacMetadataDTO;

/**
 * Service interface for Nokia NAC Metadata
 * Provides access to OpenID configuration and OAuth authorization server metadata
 */
public interface NokiaNacMetadataService {

    /**
     * Get OpenID configuration metadata
     * 
     * @return OpenID configuration metadata
     */
    GlobalResponse getOpenIdConfiguration();

    /**
     * Get security.txt content
     * 
     * @return Security.txt content
     */
    GlobalResponse getSecurityTxt();

    /**
     * Get OAuth authorization server metadata
     * 
     * @return OAuth authorization server metadata
     */
    GlobalResponse getOAuthAuthorizationServer();
}
