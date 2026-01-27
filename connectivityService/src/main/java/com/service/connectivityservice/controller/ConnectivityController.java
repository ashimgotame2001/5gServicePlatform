package com.service.connectivityservice.controller;

import com.service.shared.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/connectivity")
@RequiredArgsConstructor
public class ConnectivityController {

    @GetMapping("/health")
    @com.service.shared.annotation.MethodCode(value = "HC001", description = "Health check")
    public ResponseEntity<com.service.shared.dto.GlobalResponse> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "connectivity-service");
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }
}
