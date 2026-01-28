package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Device Connectivity Status
 * Contains device information including phone number, network access identifier,
 * and IP address information (both IPv4 and IPv6)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceConnectivityStatusDTO {

    private String phoneNumber;
    
    private String networkAccessIdentifier;
    
    private Ipv4AddressDTO ipv4Address;
    
    private String ipv6Address;

    /**
     * Nested DTO for IPv4 Address information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Ipv4AddressDTO {
        
        private String publicAddress;
        
        private String privateAddress;
        
        private Integer publicPort;
    }
}
