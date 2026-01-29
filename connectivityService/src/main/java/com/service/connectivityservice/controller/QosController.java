package com.service.connectivityservice.controller;

import com.service.shared.annotation.MethodCode;
import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateSessionRequestDTO;
import com.service.shared.dto.request.DeviceRequestDTO;
import com.service.connectivityservice.service.QosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connectivity/Qos")
@RequiredArgsConstructor
public class QosController {

    private final QosService service;

    @PostMapping("/sessions")
    @MethodCode(value = "RS001", description = "Retrieve sessions by phone number")
    public ResponseEntity<GlobalResponse> getSessionsByPhoneNumber(@RequestBody DeviceRequestDTO request){
        return ResponseEntity.ok(service.getRetrieveSessions(request));
    }

    @PostMapping("/sessions/create")
    @MethodCode(value = "CS001", description = "Create QoS session")
    public ResponseEntity<GlobalResponse> createSession(@RequestBody CreateSessionRequestDTO request){
        return ResponseEntity.ok(service.createSession(request));
    }

    @GetMapping("/sessions/{id}")
    @MethodCode(value = "RS002", description = "Retrieve sessions by session id")
    public ResponseEntity<GlobalResponse> getSessionsById(@PathVariable String id){
        return ResponseEntity.ok(service.getSession(id));
    }

}
