package com.service.identificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/identification")
@RequiredArgsConstructor
public class IdentificationController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "identification-service");
        return com.service.shared.util.ResponseHelper.successWithData("Service is healthy", healthData);
    }
}
