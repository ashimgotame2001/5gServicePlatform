package com.service.shared.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.MonitoringMetricsDTO;

/**
 * Service interface for Continuous Monitoring
 * Handles BR-7: Continuous Monitoring & Dynamic Adjustment
 */
public interface EmergencyMonitoringService {

    /**
     * Start monitoring emergency connectivity
     * 
     * @param emergencyId Emergency context ID
     * @param qosSessionId QoS session ID
     */
    GlobalResponse startMonitoring(String emergencyId, String qosSessionId);

    /**
     * Stop monitoring emergency connectivity
     */
    GlobalResponse stopMonitoring(String emergencyId);

    /**
     * Get current monitoring metrics
     */
    GlobalResponse getMonitoringMetrics(String emergencyId);

    /**
     * Check if remediation is needed
     */
    Boolean checkRemediationNeeded(MonitoringMetricsDTO metrics);

    /**
     * Trigger automatic remediation
     */
    GlobalResponse triggerRemediation(String emergencyId, MonitoringMetricsDTO metrics);

    /**
     * Get monitoring history
     */
    GlobalResponse getMonitoringHistory(String emergencyId);
}
