package com.service.identificationservice.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.PhoneVerificationRequest;

public interface IdentificationService {

    /**
     * Verify phone number
     * 
     * @param request Phone verification request
     * @return Verification result
     */
    GlobalResponse verifyPhoneNumber(PhoneVerificationRequest request);

    /**
     * Share phone number (get device phone number)
     * 
     * @return Device phone number information
     */
    GlobalResponse sharePhoneNumber();
}
