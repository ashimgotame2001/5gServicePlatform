package com.service.shared.service.impl;

import com.service.shared.dto.DecisionResultDTO;
import com.service.shared.dto.EmergencyContextDTO;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NetworkStateDTO;
import com.service.shared.dto.TrustValidationDTO;
import com.service.shared.service.EmergencyDecisionEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of Emergency Decision Engine Service
 * Handles BR-5: Autonomous Decision & Policy Evaluation
 */
@Slf4j
@Service
public class EmergencyDecisionEngineServiceImpl implements EmergencyDecisionEngineService {

    @Override
    public GlobalResponse evaluateEmergency(EmergencyContextDTO emergencyContext,
                                           TrustValidationDTO trustValidation,
                                           NetworkStateDTO networkState) {
        log.info("Evaluating emergency: {}", emergencyContext.getEmergencyId());
        
        DecisionResultDTO decisionResult = evaluatePolicyRules(emergencyContext, trustValidation, networkState);
        
        return GlobalResponse.successWithData(200, "Emergency evaluated", decisionResult);
    }

    @Override
    public DecisionResultDTO evaluatePolicyRules(EmergencyContextDTO emergencyContext,
                                                TrustValidationDTO trustValidation,
                                                NetworkStateDTO networkState) {
        log.info("Evaluating policy rules for emergency: {}", emergencyContext.getEmergencyId());
        
        // Policy evaluation logic:
        // IF emergency = TRUE AND device_role = AMBULANCE AND confidence ≥ 0.95 → APPROVE
        double confidenceScore = calculateConfidenceScore(emergencyContext, trustValidation, networkState);
        
        DecisionResultDTO.DecisionStatus status = DecisionResultDTO.DecisionStatus.APPROVED;
        if (confidenceScore < 0.95) {
            status = DecisionResultDTO.DecisionStatus.DENIED;
        } else if (confidenceScore < 0.8) {
            status = DecisionResultDTO.DecisionStatus.PENDING;
        }
        
        return DecisionResultDTO.builder()
                .decisionId(UUID.randomUUID().toString())
                .emergencyId(emergencyContext.getEmergencyId())
                .status(status)
                .confidenceScore(confidenceScore)
                .decidedAt(LocalDateTime.now())
                .explanation(generateDecisionExplanation(status, confidenceScore))
                .build();
    }

    @Override
    public Double calculateConfidenceScore(EmergencyContextDTO emergencyContext,
                                          TrustValidationDTO trustValidation,
                                          NetworkStateDTO networkState) {
        double score = 0.0;
        
        // Emergency severity factor (0.0 - 0.4)
        if (emergencyContext.getSeverity() == EmergencyContextDTO.EmergencySeverity.CRITICAL) {
            score += 0.4;
        } else if (emergencyContext.getSeverity() == EmergencyContextDTO.EmergencySeverity.HIGH) {
            score += 0.3;
        } else if (emergencyContext.getSeverity() == EmergencyContextDTO.EmergencySeverity.MEDIUM) {
            score += 0.2;
        } else {
            score += 0.1;
        }
        
        // Trust validation factor (0.0 - 0.3)
        if (trustValidation != null && trustValidation.getStatus() == TrustValidationDTO.ValidationStatus.TRUSTED) {
            score += 0.3;
        } else if (trustValidation != null && trustValidation.getTrustScore() != null) {
            score += trustValidation.getTrustScore() * 0.3;
        }
        
        // Network state factor (0.0 - 0.3)
        if (networkState != null && networkState.getQosCapacityStatus() == NetworkStateDTO.QoSCapacityStatus.AVAILABLE) {
            score += 0.3;
        } else {
            score += 0.1;
        }
        
        return Math.min(score, 1.0);
    }

    @Override
    public GlobalResponse getDecisionResult(String decisionId) {
        // In production, this would retrieve from database
        return GlobalResponse.failure(404, "Decision result not found: " + decisionId);
    }

    @Override
    public String generateDecisionExplanation(DecisionResultDTO decisionResult) {
        return String.format("Emergency decision: %s with confidence %.2f. %s",
                decisionResult.getStatus(),
                decisionResult.getConfidenceScore(),
                decisionResult.getStatus() == DecisionResultDTO.DecisionStatus.APPROVED
                        ? "Guaranteed connectivity will be activated."
                        : "Request denied due to insufficient confidence or policy constraints.");
    }
    
    private String generateDecisionExplanation(DecisionResultDTO.DecisionStatus status, double confidenceScore) {
        return String.format("Decision: %s (Confidence: %.2f)", status, confidenceScore);
    }
}
