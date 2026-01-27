package com.service.authservice.controller;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.entity.User;
import com.service.authservice.service.AuthService;
import com.service.authservice.service.OAuth2TokenValidator;
import com.service.shared.util.ResponseHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2TokenValidator tokenValidator;

    @PostMapping("/register")
    public ResponseEntity<GlobalResponse> register(@Valid @RequestBody RegisterRequest request) {
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
    public ResponseEntity<GlobalResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", token);
        loginData.put("tokenType", "Bearer");
        loginData.put("username", request.getUsername());

        return ResponseHelper.successWithData("Login successful", loginData);
    }

    @GetMapping("/validate")
    public ResponseEntity<GlobalResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseHelper.failure(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authorization header. Expected: Bearer <token>"
                );
            }

            String token = authHeader.substring(7).trim();
            
            // Validate token (signature, expiration, etc.)
            if (!tokenValidator.validateToken(token)) {
                return ResponseHelper.failure(
                    HttpStatus.UNAUTHORIZED,
                    "Token validation failed"
                );
            }
            
            // Extract claims
            String username = tokenValidator.extractUsername(token);
            String email = tokenValidator.extractEmail(token);
            Date expiration = tokenValidator.extractExpiration(token);

            Map<String, Object> validationData = new HashMap<>();
            validationData.put("valid", true);
            validationData.put("username", username);
            validationData.put("email", email);
            validationData.put("expiresAt", expiration.getTime());

            return ResponseHelper.successWithData("Token is valid", validationData);
        } catch (Exception e) {
            return ResponseHelper.failure(
                HttpStatus.UNAUTHORIZED,
                "Token validation failed: " + e.getMessage()
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<GlobalResponse> health() {
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
