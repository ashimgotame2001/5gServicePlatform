package com.service.shared.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.NetworkStateDTO;

/**
 * Service interface for Real-Time Network State Assessment
 * Handles BR-4: Real-Time Network State Assessment
 */
public interface NetworkStateAssessmentService {

    /**
     * Assess network state for a given location
     * 
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Network state assessment result
     */
    GlobalResponse assessNetworkState(Double latitude, Double longitude);

    /**
     * Assess network state for a region
     * 
     * @param regionId Region identifier
     * @return Network state assessment result
     */
    GlobalResponse assessNetworkStateByRegion(String regionId);

    /**
     * Check congestion level for a location
     */
    GlobalResponse checkCongestionLevel(Double latitude, Double longitude);

    /**
     * Check available network slices
     */
    GlobalResponse checkAvailableSlices(Double latitude, Double longitude);

    /**
     * Check QoS capacity
     */
    GlobalResponse checkQoSCapacity(Double latitude, Double longitude);

    /**
     * Predict impact of prioritization
     */
    GlobalResponse predictPrioritizationImpact(Double latitude, Double longitude, 
                                                String qosProfile, String sliceType);
}
