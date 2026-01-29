package com.service.locationservice.controller;

import com.service.locationservice.service.LocationService;
import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.LocationRetrievalDTO;
import com.service.shared.dto.request.LocationVerificationDto;
import com.service.shared.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/health")
    @MethodCode(value = "HC001", description = "Health check")
    public ResponseEntity<GlobalResponse> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "location-service");
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }

    @PostMapping("/verify")
    @MethodCode(value = "LV001", description = "Verify device location")
    public ResponseEntity<GlobalResponse> verifyLocation(
            @RequestBody LocationVerificationDto request,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        return ResponseEntity.ok(locationService.verifyLocation(request, version));
    }

    @PostMapping("/verify/v1")
    @MethodCode(value = "LV002", description = "Verify device location (v1)")
    public ResponseEntity<GlobalResponse> verifyLocationV1(
            @RequestBody LocationVerificationDto request) {
        return ResponseEntity.ok(locationService.verifyLocation(request, "v1"));
    }

    @PostMapping("/verify/v2")
    @MethodCode(value = "LV003", description = "Verify device location (v2)")
    public ResponseEntity<GlobalResponse> verifyLocationV2(
            @RequestBody LocationVerificationDto request) {
        return ResponseEntity.ok(locationService.verifyLocation(request, "v2"));
    }

    @PostMapping("/verify/v3")
    @MethodCode(value = "LV004", description = "Verify device location (v3)")
    public ResponseEntity<GlobalResponse> verifyLocationV3(
            @RequestBody LocationVerificationDto request) {
        return ResponseEntity.ok(locationService.verifyLocation(request, "v3"));
    }

    @PostMapping("/retrieve")
    @MethodCode(value = "LR001", description = "Retrieve device location")
    public ResponseEntity<GlobalResponse> retrieveLocation(
            @RequestBody LocationRetrievalDTO request) {
        return ResponseEntity.ok(locationService.retrieveLocation(request));
    }
}
