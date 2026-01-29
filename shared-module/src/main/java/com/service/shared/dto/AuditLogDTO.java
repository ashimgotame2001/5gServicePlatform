package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for Audit & Compliance Logging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogDTO {

    /**
     * Audit log ID (UUID)
     */
    private String auditId;

    /**
     * Emergency context ID (if applicable)
     */
    private String emergencyId;

    /**
     * Event type: DECISION, API_CALL, NETWORK_CHANGE, ROLLBACK
     */
    private AuditEventType eventType;

    /**
     * Service that generated the audit log
     */
    private String sourceService;

    /**
     * Action performed
     */
    private String action;

    /**
     * Action result: SUCCESS, FAILURE, PARTIAL
     */
    private ActionResult result;

    /**
     * User/System that initiated the action
     */
    private String initiatedBy;

    /**
     * Timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Decision details (if eventType is DECISION)
     */
    private DecisionResultDTO decisionDetails;

    /**
     * API invocation details (if eventType is API_CALL)
     */
    private ApiInvocationDTO apiInvocation;

    /**
     * Network change details (if eventType is NETWORK_CHANGE)
     */
    private NetworkChangeDTO networkChange;

    /**
     * Additional audit metadata
     */
    private Map<String, Object> metadata;

    /**
     * Compliance tags (e.g., SLA_REQUIRED, REGULATORY_REQUIRED)
     */
    private java.util.List<String> complianceTags;

    public enum AuditEventType {
        DECISION,
        API_CALL,
        NETWORK_CHANGE,
        ROLLBACK,
        TRUST_VALIDATION,
        NETWORK_ASSESSMENT
    }

    public enum ActionResult {
        SUCCESS,
        FAILURE,
        PARTIAL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiInvocationDTO {
        private String apiEndpoint;
        private String httpMethod;
        private Integer httpStatusCode;
        private Long responseTimeMs;
        private String requestBody;
        private String responseBody;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NetworkChangeDTO {
        private String changeType; // QOS_ACTIVATION, SLICE_ASSIGNMENT, TRAFFIC_PRIORITIZATION
        private String changeId;
        private String previousState;
        private String newState;
        private LocalDateTime changeTimestamp;
    }
}
