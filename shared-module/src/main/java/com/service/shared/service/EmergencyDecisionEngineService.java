package com.service.shared.service;

import com.service.shared.dto.DecisionResultDTO;
import com.service.shared.dto.EmergencyContextDTO;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NetworkStateDTO;
import com.service.shared.dto.TrustValidationDTO;

/**
 * Service interface for AI Decision Engine
 * Handles BR-5: Autonomous Decision & Policy Evaluation
 */
public interface EmergencyDecisionEngineService {

    /**
     * Evaluate emergency and make autonomous decision
     * 
     * @param emergencyContext Emergency context
     * @param trustValidation Trust validation result
     * @param networkState Network state assessment
     * @return Decision result
     */
    GlobalResponse evaluateEmergency(EmergencyContextDTO emergencyContext,
                                     TrustValidationDTO trustValidation,
                                     NetworkStateDTO networkState);

    /**
     * Evaluate policy rules for emergency
     */
    DecisionResultDTO evaluatePolicyRules(EmergencyContextDTO emergencyContext,
                                           TrustValidationDTO trustValidation,
                                           NetworkStateDTO networkState);

    /**
     * Calculate confidence score
     */
    Double calculateConfidenceScore(EmergencyContextDTO emergencyContext,
                                     TrustValidationDTO trustValidation,
                                     NetworkStateDTO networkState);

    /**
     * Get decision result by ID
     */
    GlobalResponse getDecisionResult(String decisionId);

    /**
     * Get decision explanation
     */
    String generateDecisionExplanation(DecisionResultDTO decisionResult);
}
