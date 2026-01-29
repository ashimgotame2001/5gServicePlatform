package com.service.shared.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.shared.dto.DeviceConnectivityStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for creating a network slice subscription
 * Based on CAMARA Project network slice subscription API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateNetworkSliceSubscriptionDTO {

    /**
     * Protocol for the subscription (e.g., "HTTP")
     */
    private String protocol;

    /**
     * Sink URL where notifications will be sent
     */
    private String sink;

    /**
     * Credentials for the sink endpoint
     */
    private SinkCredentialDTO sinkCredential;

    /**
     * List of event types to subscribe to
     */
    private List<String> types;

    /**
     * Subscription configuration
     */
    private NetworkSliceConfigDTO config;

    /**
     * DTO for sink credentials
     * Supports PLAIN and ACCESSTOKEN credential types
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SinkCredentialDTO {
        
        /**
         * Credential type: "PLAIN" or "ACCESSTOKEN"
         */
        private String credentialType;
        
        // For PLAIN credentials
        private String identifier;
        private String secret;
        
        // For ACCESSTOKEN credentials
        private String accessToken;
        private String accessTokenExpiresUtc; // ISO 8601 date-time format
        private String accessTokenType; // "bearer"
    }

    /**
     * DTO for network slice subscription configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NetworkSliceConfigDTO {
        
        /**
         * Subscription details including device and slice information
         */
        private NetworkSliceSubscriptionDetailDTO subscriptionDetail;
        
        /**
         * Subscription expiration time (ISO 8601 date-time format)
         */
        private String subscriptionExpireTime;
        
        /**
         * Maximum number of events to report
         */
        private Integer subscriptionMaxEvents;
        
        /**
         * Whether to send initial event when subscription is created
         */
        private Boolean initialEvent;
    }

    /**
     * DTO for network slice subscription details
     * Reuses DeviceConnectivityStatusDTO for device information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NetworkSliceSubscriptionDetailDTO {
        
        /**
         * Device information - reusing DeviceConnectivityStatusDTO
         */
        private DeviceConnectivityStatusDTO device;
        
        /**
         * Network slice information
         */
        private NetworkSliceInfoDTO sliceInfo;
    }

    /**
     * DTO for network slice information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NetworkSliceInfoDTO {
        
        /**
         * Slice ID
         */
        private String sliceId;
        
        /**
         * Slice type (e.g., "eMBB", "uRLLC", "mIoT")
         */
        private String sliceType;
        
        /**
         * Slice service type
         */
        private String serviceType;
        
        /**
         * Additional slice parameters
         */
        private Map<String, Object> parameters;
    }
}
