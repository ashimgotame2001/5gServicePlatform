package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Continuous Monitoring Metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitoringMetricsDTO {

    /**
     * Emergency context ID
     */
    private String emergencyId;

    /**
     * QoS session ID
     */
    private String qosSessionId;

    /**
     * Current latency in milliseconds
     */
    private Double latencyMs;

    /**
     * Current jitter in milliseconds
     */
    private Double jitterMs;

    /**
     * Packet loss percentage (0-100)
     */
    private Double packetLossPercentage;

    /**
     * Throughput in Mbps
     */
    private Double throughputMbps;

    /**
     * Connection status: CONNECTED, DEGRADED, DISCONNECTED
     */
    private ConnectionStatus connectionStatus;

    /**
     * Health score (0.0 - 1.0)
     */
    private Double healthScore;

    /**
     * Metrics timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Whether automatic remediation was triggered
     */
    private Boolean remediationTriggered;

    /**
     * Remediation actions taken
     */
    private String remediationActions;

    public enum ConnectionStatus {
        CONNECTED,
        DEGRADED,
        DISCONNECTED,
        UNKNOWN
    }
}
