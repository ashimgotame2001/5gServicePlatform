package com.service.shared.dto.request;

import lombok.Data;

@Data
public class PhoneVerificationRequest {

    private String phoneNumber;
    private String hashedPhoneNumber;
}
