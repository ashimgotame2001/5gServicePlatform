package com.service.connectivityservice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for device information in session request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceInfoDTO {
    
    /**
     * Phone number in E.164 format (e.g., +99999991001)
     */
    private String phoneNumber;
    
    /**
     * IPv4 address information
     */
    private Ipv4AddressDTO ipv4Address;
}
