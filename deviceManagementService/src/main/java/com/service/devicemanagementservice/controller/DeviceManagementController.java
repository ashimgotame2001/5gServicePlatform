package com.service.devicemanagementservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceManagementController {

    @GetMapping("/health")
    public ResponseEntity<com.service.shared.dto.GlobalResponse> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "device-management-service");
        return com.service.shared.util.ResponseHelper.successWithData("Service is healthy", healthData);
    }




}
