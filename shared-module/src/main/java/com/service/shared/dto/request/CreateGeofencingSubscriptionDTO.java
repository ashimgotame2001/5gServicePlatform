package com.service.shared.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.shared.dto.DeviceConnectivityStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a geofencing subscription
 * Based on CAMARA Project geofencing subscription API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateGeofencingSubscriptionDTO {

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
     * e.g., "org.camaraproject.geofencing-subscriptions.v0.area-entered"
     * e.g., "org.camaraproject.geofencing-subscriptions.v0.area-left"
     */
    private List<String> types;

    /**
     * Subscription configuration
     */
    private SubscriptionConfigDTO config;

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
     * DTO for subscription configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubscriptionConfigDTO {
        
        /**
         * Subscription details including device and area
         */
        private GeofencingSubscriptionDetailDTO subscriptionDetail;
        
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
     * DTO for geofencing subscription details
     * Reuses DeviceConnectivityStatusDTO for device information
     * Uses GeofencingAreaDTO for area definition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeofencingSubscriptionDetailDTO {
        
        /**
         * Device information - reusing DeviceConnectivityStatusDTO
         */
        private DeviceConnectivityStatusDTO device;
        
        /**
         * Geofencing area definition
         */
        private GeofencingAreaDTO area;
    }

    /**
     * DTO for geofencing area
     * Simplified version for geofencing subscriptions (CIRCLE type only)
     * Reuses CenterDTO concept from AreaDTO but with simpler structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeofencingAreaDTO {
        
        /**
         * Area type - "CIRCLE" for geofencing subscriptions
         * Default: "CIRCLE"
         */
        private String areaType;
        
        /**
         * Center point of the area
         * Reusing the CenterDTO structure from AreaDTO
         */
        private CenterDTO center;
        
        /**
         * Radius in meters (1-200000)
         */
        private Number radius; // Can be Integer or Double

        /**
         * Center point DTO
         * Reusing structure from AreaDTO.CenterDTO
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CenterDTO {
            
            /**
             * Latitude coordinate (-90 to 90)
             */
            private Double latitude;
            
            /**
             * Longitude coordinate (-180 to 180)
             */
            private Double longitude;
        }
    }
}
