package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for Real-Time Network State Assessment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkStateDTO {

    /**
     * Geographic region identifier
     */
    private String regionId;

    /**
     * Latitude for location-based assessment
     */
    private Double latitude;

    /**
     * Longitude for location-based assessment
     */
    private Double longitude;

    /**
     * Current congestion level: LOW, MEDIUM, HIGH, CRITICAL
     */
    private CongestionLevel congestionLevel;

    /**
     * Congestion percentage (0-100)
     */
    private Double congestionPercentage;

    /**
     * Available network slices
     */
    private List<NetworkSliceDTO> availableSlices;

    /**
     * QoS capacity status: AVAILABLE, LIMITED, UNAVAILABLE
     */
    private QoSCapacityStatus qosCapacityStatus;

    /**
     * Available QoS capacity percentage (0-100)
     */
    private Double qosCapacityPercentage;

    /**
     * Predicted impact of prioritization (0-100, higher = more impact)
     */
    private Double prioritizationImpactScore;

    /**
     * Network status timestamp
     */
    private LocalDateTime assessedAt;

    /**
     * Additional network metrics
     */
    private Map<String, Object> networkMetrics;

    public enum CongestionLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum QoSCapacityStatus {
        AVAILABLE,
        LIMITED,
        UNAVAILABLE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NetworkSliceDTO {
        private String sliceId;
        private String sliceType;
        private Integer availableCapacity;
        private Integer totalCapacity;
        private String status; // AVAILABLE, RESERVED, FULL
    }
}
