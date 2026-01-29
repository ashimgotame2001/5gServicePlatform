package com.service.locationservice.controller;

import com.service.locationservice.service.GeofencingSubscriptionService;
import com.service.locationservice.service.LocationService;
import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateGeofencingSubscriptionDTO;
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
    private final GeofencingSubscriptionService geofencingSubscriptionService;

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

    @PostMapping("/geofencing/subscriptions")
    @MethodCode(value = "GF001", description = "Create geofencing subscription")
    public ResponseEntity<GlobalResponse> createGeofencingSubscription(
            @RequestBody CreateGeofencingSubscriptionDTO request) {
        return ResponseEntity.ok(geofencingSubscriptionService.createGeofencingSubscription(request));
    }

    @GetMapping("/geofencing/subscriptions")
    @MethodCode(value = "GF002", description = "Get all geofencing subscriptions")
    public ResponseEntity<GlobalResponse> getAllGeofencingSubscriptions() {
        return ResponseEntity.ok(geofencingSubscriptionService.getAllGeofencingSubscriptions());
    }

    @GetMapping("/geofencing/subscriptions/{subscriptionId}")
    @MethodCode(value = "GF003", description = "Get geofencing subscription by ID")
    public ResponseEntity<GlobalResponse> getGeofencingSubscriptionById(
            @PathVariable String subscriptionId) {
        return ResponseEntity.ok(geofencingSubscriptionService.getGeofencingSubscriptionById(subscriptionId));
    }

    @DeleteMapping("/geofencing/subscriptions/{subscriptionId}")
    @MethodCode(value = "GF004", description = "Delete geofencing subscription")
    public ResponseEntity<GlobalResponse> deleteGeofencingSubscription(
            @PathVariable String subscriptionId) {
        return ResponseEntity.ok(geofencingSubscriptionService.deleteGeofencingSubscription(subscriptionId));
    }
}
