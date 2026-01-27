package com.service.locationservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "location-service");
        return com.service.shared.util.ResponseHelper.successWithData("Service is healthy", healthData);
    }
}
