package com.service.authservice.controller;

import com.service.authservice.dto.GlobalResponse;
import com.service.authservice.entity.User;
import com.service.authservice.service.AuthService;
import com.service.authservice.service.UserService;
import com.service.authservice.util.JwtUtil;
import com.service.authservice.util.ResponseHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            request.getDateOfBirth(),
            request.getPhoneNumber(),
            request.getDeviceImei(),
            request.getDeviceModel(),
            request.getDeviceManufacturer(),
            request.getDeviceOs(),
            request.getDeviceOsVersion(),
            request.getAddressLine1(),
            request.getAddressLine2(),
            request.getCity(),
            request.getState(),
            request.getCountry(),
            request.getPostalCode(),
            request.getIdDocumentType(),
            request.getIdDocumentNumber(),
            request.getSimCardNumber(),
            request.getSimCardType()
        );

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("deviceImei", user.getDeviceImei());
        userData.put("fullName", user.getFullName());

        return ResponseHelper.successWithData(
            HttpStatus.CREATED,
            "User registered successfully",
            userData
        );
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", token);
        loginData.put("tokenType", "Bearer");
        loginData.put("username", request.getUsername());

        return ResponseHelper.successWithData("Login successful", loginData);
    }

    @GetMapping("/validate")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseHelper.failure(
                HttpStatus.UNAUTHORIZED,
                "Invalid authorization header"
            );
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        String email = jwtUtil.extractEmail(token);

        Map<String, Object> validationData = new HashMap<>();
        validationData.put("valid", true);
        validationData.put("username", username);
        validationData.put("email", email);

        return ResponseHelper.successWithData("Token is valid", validationData);
    }

    @GetMapping("/health")
    public ResponseEntity<GlobalResponse<Map<String, String>>> health() {
        Map<String, String> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "auth-service");
        return ResponseHelper.successWithData("Service is healthy", healthData);
    }

    @Data
    static class RegisterRequest {
        // Basic Information
        @NotBlank
        private String username;
        
        @NotBlank
        @Email
        private String email;
        
        @NotBlank
        @Size(min = 6)
        private String password;
        
        // KYC Information
        private String firstName;
        private String lastName;
        private java.time.LocalDate dateOfBirth;
        private String idDocumentType; // PASSPORT, DRIVERS_LICENSE, NATIONAL_ID
        private String idDocumentNumber;
        
        // Location Information
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        
        // Phone Number (for Number Verification, SIM Swap)
        private String phoneNumber;
        
        // Device Information (for Device Status, Device Swap)
        private String deviceImei;
        private String deviceModel;
        private String deviceManufacturer;
        private String deviceOs; // iOS, Android
        private String deviceOsVersion;
        
        // SIM Card Information (for SIM Swap)
        private String simCardNumber; // ICCID
        private String simCardType; // PHYSICAL, eSIM
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
