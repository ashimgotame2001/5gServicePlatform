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
 * DTO for AI Decision Engine Result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionResultDTO {

    /**
     * Decision ID (UUID)
     */
    private String decisionId;

    /**
     * Emergency context ID
     */
    private String emergencyId;

    /**
     * Decision: APPROVED, DENIED, PENDING
     */
    private DecisionStatus status;

    /**
     * Confidence score (0.0 - 1.0)
     */
    private Double confidenceScore;

    /**
     * Decision timestamp
     */
    private LocalDateTime decidedAt;

    /**
     * Policy rules evaluated
     */
    private List<PolicyRuleDTO> evaluatedRules;

    /**
     * Decision explanation/reasoning
     */
    private String explanation;

    /**
     * Recommended QoS profile
     */
    private String recommendedQosProfile;

    /**
     * Recommended network slice
     */
    private String recommendedSliceId;

    /**
     * Risk assessment score (0.0 - 1.0, higher = more risk)
     */
    private Double riskScore;

    /**
     * Additional decision metadata
     */
    private Map<String, Object> metadata;

    public enum DecisionStatus {
        APPROVED,
        DENIED,
        PENDING,
        REQUIRES_MANUAL_REVIEW
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PolicyRuleDTO {
        private String ruleId;
        private String ruleName;
        private Boolean passed;
        private String reason;
        private Double weight;
    }
}
