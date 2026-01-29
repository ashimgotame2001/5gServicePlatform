package com.service.shared.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for IPv4 address information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ipv4AddressDTO {
    
    /**
     * Public IPv4 address
     */
    private String publicAddress;
    
    /**
     * Private IPv4 address
     */
    private String privateAddress;
    
    /**
     * Public port number
     */
    private Integer publicPort;
}
