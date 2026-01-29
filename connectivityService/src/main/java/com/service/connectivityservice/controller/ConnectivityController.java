package com.service.connectivityservice.controller;

import com.service.connectivityservice.service.NetworkSliceService;
import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateNetworkSliceSubscriptionDTO;
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

    private final NetworkSliceService networkSliceService;

    @GetMapping("/health")
    @MethodCode(value = "HC001", description = "Health check")
    public ResponseEntity<GlobalResponse> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "connectivity-service");
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }

    @PostMapping("/network-slice/subscriptions")
    @MethodCode(value = "NS001", description = "Create network slice subscription")
    public ResponseEntity<GlobalResponse> createNetworkSliceSubscription(
            @RequestBody CreateNetworkSliceSubscriptionDTO request) {
        return ResponseEntity.ok(networkSliceService.createNetworkSliceSubscription(request));
    }

    @GetMapping("/network-slice/subscriptions")
    @MethodCode(value = "NS002", description = "Get all network slice subscriptions")
    public ResponseEntity<GlobalResponse> getAllNetworkSliceSubscriptions() {
        return ResponseEntity.ok(networkSliceService.getAllNetworkSliceSubscriptions());
    }

    @GetMapping("/network-slice/subscriptions/{subscriptionId}")
    @MethodCode(value = "NS003", description = "Get network slice subscription by ID")
    public ResponseEntity<GlobalResponse> getNetworkSliceSubscriptionById(
            @PathVariable String subscriptionId) {
        return ResponseEntity.ok(networkSliceService.getNetworkSliceSubscriptionById(subscriptionId));
    }

    @DeleteMapping("/network-slice/subscriptions/{subscriptionId}")
    @MethodCode(value = "NS004", description = "Delete network slice subscription")
    public ResponseEntity<GlobalResponse> deleteNetworkSliceSubscription(
            @PathVariable String subscriptionId) {
        return ResponseEntity.ok(networkSliceService.deleteNetworkSliceSubscription(subscriptionId));
    }
}
