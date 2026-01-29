package com.service.identificationservice.client;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface NokiaNocPhoneNumberVerificationClient {

    Mono<Map<String,Object>> verifyPhoneNumber(com.service.shared.dto.request.PhoneVerificationRequest request);
    Mono<Map<String,Object>> sharePhoneNumber();

}
