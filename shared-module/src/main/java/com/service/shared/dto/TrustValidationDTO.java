package com.service.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Trust & Authorization Validation Result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrustValidationDTO {

    /**
     * Phone number being validated
     */
    private String phoneNumber;

    /**
     * Device IMEI
     */
    private String deviceImei;

    /**
     * SIM card number (ICCID)
     */
    private String simCardNumber;

    /**
     * Validation result: TRUSTED, UNTRUSTED, SUSPICIOUS
     */
    private ValidationStatus status;

    /**
     * Device role verification result
     */
    private EmergencyContextDTO.DeviceRole verifiedRole;

    /**
     * SIM/eSIM integrity check result
     */
    private Boolean simIntegrityValid;

    /**
     * Device identity verification result
     */
    private Boolean deviceIdentityValid;

    /**
     * Overall trust score (0.0 - 1.0)
     */
    private Double trustScore;

    /**
     * Validation timestamp
     */
    private LocalDateTime validatedAt;

    /**
     * Reason for rejection (if status is UNTRUSTED or SUSPICIOUS)
     */
    private String rejectionReason;

    public enum ValidationStatus {
        TRUSTED,
        UNTRUSTED,
        SUSPICIOUS,
        PENDING
    }
}
