package com.service.decisionengineservice.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Network data collected from real 5G network sources
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkData {
    
    /**
     * Connectivity metrics
     */
    @Builder.Default
    private ConnectivityMetrics connectivity = new ConnectivityMetrics();
    
    /**
     * Location data
     */
    @Builder.Default
    private LocationData location = new LocationData();
    
    /**
     * Device status information
     */
    @Builder.Default
    private DeviceStatus deviceStatus = new DeviceStatus();
    
    /**
     * QoS metrics
     */
    @Builder.Default
    private QoSMetrics qos = new QoSMetrics();
    
    /**
     * Additional raw data from Nokia APIs
     */
    @Builder.Default
    private Map<String, Object> rawData = new HashMap<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectivityMetrics {
        private String status;
        private Integer signalStrength;
        private String networkType; // 5G, 4G, etc.
        private Double latency;
        private Double throughput;
        private Boolean isConnected;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private String locationType;
        private Integer maxAge;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatus {
        private String deviceId;
        private String imei;
        private String simCardNumber;
        private String status;
        private String deviceType;
        private Boolean isActive;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QoSMetrics {
        private String qosProfile;
        private Integer priority;
        private Double bandwidth;
        private Double latency;
        private Boolean isGuaranteed;
    }
}
