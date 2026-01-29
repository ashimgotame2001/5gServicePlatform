package com.service.shared.client;

import com.service.shared.dto.NokiaNacClientCredentialsDTO;
import reactor.core.publisher.Mono;

/**
 * Client for retrieving Nokia NAC client credentials from authorization server
 */
public interface NokiaNacClientCredentialsClient {

    /**
     * Retrieve client credentials (client_id and client_secret) from Nokia NAC authorization server
     * 
     * @return Mono containing client credentials
     */
    Mono<NokiaNacClientCredentialsDTO> getClientCredentials();
}
