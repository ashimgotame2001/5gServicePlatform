package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Network Orchestration Execution Result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkOrchestrationDTO {

    /**
     * Orchestration ID (UUID)
     */
    private String orchestrationId;

    /**
     * Emergency context ID
     */
    private String emergencyId;

    /**
     * Decision ID that triggered this orchestration
     */
    private String decisionId;

    /**
     * Execution status: SUCCESS, FAILED, IN_PROGRESS, ROLLED_BACK
     */
    private ExecutionStatus status;

    /**
     * QoS session ID (if created)
     */
    private String qosSessionId;

    /**
     * Network slice ID (if assigned)
     */
    private String sliceId;

    /**
     * Execution timestamp
     */
    private LocalDateTime executedAt;

    /**
     * Completion timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Execution duration in milliseconds
     */
    private Long executionDurationMs;

    /**
     * Error message (if failed)
     */
    private String errorMessage;

    /**
     * Rollback status: NOT_ROLLED_BACK, ROLLED_BACK, ROLLBACK_FAILED
     */
    private RollbackStatus rollbackStatus;

    /**
     * Rollback timestamp
     */
    private LocalDateTime rolledBackAt;

    public enum ExecutionStatus {
        SUCCESS,
        FAILED,
        IN_PROGRESS,
        ROLLED_BACK
    }

    public enum RollbackStatus {
        NOT_ROLLED_BACK,
        ROLLED_BACK,
        ROLLBACK_FAILED
    }
}
