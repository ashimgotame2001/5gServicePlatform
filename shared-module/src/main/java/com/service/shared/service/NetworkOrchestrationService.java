package com.service.shared.service;

import com.service.shared.dto.DecisionResultDTO;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NetworkOrchestrationDTO;

/**
 * Service interface for Network Orchestration
 * Handles BR-6: Guaranteed Connectivity Execution
 */
public interface NetworkOrchestrationService {

    /**
     * Execute guaranteed connectivity based on decision
     * 
     * @param decisionResult Decision result from AI engine
     * @return Orchestration result
     */
    GlobalResponse executeGuaranteedConnectivity(DecisionResultDTO decisionResult);

    /**
     * Request QoS on Demand
     */
    GlobalResponse requestQoSOnDemand(String phoneNumber, String qosProfile, Integer duration);

    /**
     * Assign priority network slice
     */
    GlobalResponse assignNetworkSlice(String phoneNumber, String sliceId);

    /**
     * Enable traffic preemption
     */
    GlobalResponse enableTrafficPreemption(String phoneNumber, String qosSessionId);

    /**
     * Get orchestration status
     */
    GlobalResponse getOrchestrationStatus(String orchestrationId);

    /**
     * Rollback network changes
     */
    GlobalResponse rollbackNetworkChanges(String orchestrationId, String emergencyId);
}
