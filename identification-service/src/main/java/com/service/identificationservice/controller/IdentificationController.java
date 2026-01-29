package com.service.identificationservice.controller;

import com.service.identificationservice.service.IdentificationService;
import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.PhoneVerificationRequest;
import com.service.shared.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/identification")
@RequiredArgsConstructor
public class IdentificationController {

    private final IdentificationService identificationService;

    @GetMapping("/health")
    @MethodCode(value = "HC001", description = "Health check")
    public ResponseEntity<GlobalResponse> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "identification-service");
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }

    @PostMapping("/verify-number")
    @MethodCode(value = "IV001", description = "Verify phone number")
    public ResponseEntity<GlobalResponse> verifyPhoneNumber(
            @RequestBody PhoneVerificationRequest request) {
        return ResponseEntity.ok(identificationService.verifyPhoneNumber(request));
    }

    @GetMapping("/share-phone-number")
    @MethodCode(value = "IV002", description = "Get device phone number")
    public ResponseEntity<GlobalResponse> sharePhoneNumber() {
        return ResponseEntity.ok(identificationService.sharePhoneNumber());
    }
}
