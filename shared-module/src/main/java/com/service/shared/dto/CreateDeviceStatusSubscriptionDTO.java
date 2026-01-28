package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a device status subscription
 * Contains subscription details, expiration time, and webhook configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateDeviceStatusSubscriptionDTO {

    private SubscriptionDetailDTO subscriptionDetail;
    
    private String subscriptionExpireTime; // ISO 8601 date-time format
    
    private WebhookDTO webhook;

    /**
     * Nested DTO for subscription details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubscriptionDetailDTO {
        
        private DeviceConnectivityStatusDTO device;
        
        private String type; // e.g., "org.camaraproject.device-status.v0.roaming-status"
    }

    /**
     * Nested DTO for webhook configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WebhookDTO {
        
        private String notificationUrl;
        
        private String notificationAuthToken;
    }
}
