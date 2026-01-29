package com.service.shared.service;

import com.service.shared.dto.AuditLogDTO;
import com.service.shared.dto.GlobalResponse;

/**
 * Service interface for Audit & Compliance
 * Handles BR-8: Audit & Compliance Logging
 */
public interface AuditService {

    /**
     * Log audit event
     */
    GlobalResponse logAuditEvent(AuditLogDTO auditLog);

    /**
     * Log decision audit
     */
    GlobalResponse logDecision(String emergencyId, String decisionId, 
                               AuditLogDTO.AuditEventType eventType, 
                               AuditLogDTO.ActionResult result);

    /**
     * Log API invocation
     */
    GlobalResponse logApiInvocation(String emergencyId, String endpoint, 
                                    String method, Integer statusCode, 
                                    Long responseTimeMs);

    /**
     * Log network change
     */
    GlobalResponse logNetworkChange(String emergencyId, String changeType, 
                                    String changeId, String previousState, 
                                    String newState);

    /**
     * Get audit logs by emergency ID
     */
    GlobalResponse getAuditLogsByEmergency(String emergencyId);

    /**
     * Get audit logs by date range
     */
    GlobalResponse getAuditLogsByDateRange(java.time.LocalDateTime startDate, 
                                           java.time.LocalDateTime endDate);

    /**
     * Generate compliance report
     */
    GlobalResponse generateComplianceReport(String emergencyId);
}
