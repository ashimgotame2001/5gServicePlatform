package com.service.devicemanagementservice.controller;

import com.service.devicemanagementservice.service.DeviceStatusService;
import com.service.devicemanagementservice.service.DeviceSwapService;
import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.CreateDeviceStatusSubscriptionDTO;
import com.service.shared.dto.DeviceConnectivityStatusDTO;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CheckDeviceSwap;
import com.service.shared.dto.request.DeviceDTO;
import com.service.shared.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceManagementController {

    private final DeviceStatusService deviceStatusService;
    private final DeviceSwapService deviceSwapService;

    @GetMapping("/health")
    @MethodCode(value = "HC001", description = "Health check")
    public ResponseEntity<GlobalResponse> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "device-management-service");
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }

    @PostMapping("/status/connectivity")
    @MethodCode(value = "DS001", description = "Get device connectivity status")
    public ResponseEntity<GlobalResponse> getDeviceConnectivityStatus(
            @RequestBody DeviceConnectivityStatusDTO status) {
        return ResponseEntity.ok(deviceStatusService.getDeviceConnectivityStatus(status));
    }

    @PostMapping("/status/roaming")
    @MethodCode(value = "DS002", description = "Get device roaming status")
    public ResponseEntity<GlobalResponse> getDeviceRoamingStatus(
            @RequestBody DeviceConnectivityStatusDTO status) {
        return ResponseEntity.ok(deviceStatusService.getDeviceRoamingStatus(status));
    }

    @GetMapping("/subscriptions")
    @MethodCode(value = "DS003", description = "Get all device status subscriptions")
    public ResponseEntity<GlobalResponse> getAllSubscriptions() {
        return ResponseEntity.ok(deviceStatusService.getAllSubscriptions());
    }

    @PostMapping("/subscriptions")
    @MethodCode(value = "DS004", description = "Create device status subscription")
    public ResponseEntity<GlobalResponse> createDeviceStatusSubscription(
            @RequestBody CreateDeviceStatusSubscriptionDTO request) {
        return ResponseEntity.ok(deviceStatusService.createDeviceStatusSubscription(request));
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    @MethodCode(value = "DS005", description = "Get subscription by ID")
    public ResponseEntity<GlobalResponse> getSubscriptionById(
            @PathVariable String subscriptionId) {
        return ResponseEntity.ok(deviceStatusService.getSubscriptionById(subscriptionId));
    }

    @PostMapping("/swap/retrieve-date")
    @MethodCode(value = "DSW001", description = "Retrieve device swap date")
    public ResponseEntity<GlobalResponse> retrieveDeviceSwapDate(
            @RequestBody DeviceDTO device) {
        return ResponseEntity.ok(deviceSwapService.retrieveDeviceSwapDate(device));
    }

    @PostMapping("/swap/check")
    @MethodCode(value = "DSW002", description = "Check device swap")
    public ResponseEntity<GlobalResponse> checkDeviceSwap(
            @RequestBody CheckDeviceSwap swap) {
        return ResponseEntity.ok(deviceSwapService.checkDeviceSwap(swap));
    }
}
