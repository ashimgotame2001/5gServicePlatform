package com.service.shared.service;

import com.service.shared.dto.EmergencyContextDTO;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.TrustValidationDTO;

/**
 * Service interface for Trust & Authorization Validation
 * Handles BR-3: Trust & Authorization Validation
 */
public interface TrustValidationService {

    /**
     * Validate device trust and authorization for emergency priority connectivity
     * 
     * @param phoneNumber Device phone number
     * @param deviceImei Device IMEI
     * @param deviceRole Expected device role
     * @return Trust validation result
     */
    GlobalResponse validateDeviceTrust(String phoneNumber, String deviceImei, 
                                       EmergencyContextDTO.DeviceRole deviceRole);

    /**
     * Verify SIM/eSIM integrity
     */
    GlobalResponse verifySimIntegrity(String phoneNumber, String simCardNumber);

    /**
     * Verify device identity
     */
    GlobalResponse verifyDeviceIdentity(String phoneNumber, String deviceImei);

    /**
     * Get trust validation result by ID
     */
    GlobalResponse getTrustValidation(String validationId);

    /**
     * Calculate trust score for a device
     */
    Double calculateTrustScore(String phoneNumber, String deviceImei, String simCardNumber);
}
