package com.service.shared.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "phoneNumber"),
    @UniqueConstraint(columnNames = "deviceImei"),
    @UniqueConstraint(columnNames = "simCardNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    // KYC (Know Your Customer) Information
    @Column(name = "first_name")
    @Size(max = 100)
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "id_document_type")
    @Size(max = 50)
    private String idDocumentType; // PASSPORT, DRIVERS_LICENSE, NATIONAL_ID, etc.

    @Column(name = "id_document_number")
    @Size(max = 100)
    private String idDocumentNumber;

    @Column(name = "kyc_verified")
    private Boolean kycVerified = false;

    @Column(name = "kyc_verified_at")
    private LocalDateTime kycVerifiedAt;

    // Location Information (for Location Verification API)
    @Column(name = "address_line1")
    @Size(max = 255)
    private String addressLine1;

    @Column(name = "address_line2")
    @Size(max = 255)
    private String addressLine2;

    @Column(name = "city")
    @Size(max = 100)
    private String city;

    @Column(name = "state")
    @Size(max = 100)
    private String state;

    @Column(name = "country")
    @Size(max = 100)
    private String country;

    @Column(name = "postal_code")
    @Size(max = 20)
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Phone Number (for Number Verification, SIM Swap)
    @Column(name = "phone_number", unique = true)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20)
    private String phoneNumber;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    // Device Information (for Device Status, Device Swap)
    @Column(name = "device_imei", unique = true)
    @Size(max = 15)
    private String deviceImei; // International Mobile Equipment Identity

    @Column(name = "device_model")
    @Size(max = 100)
    private String deviceModel;

    @Column(name = "device_manufacturer")
    @Size(max = 100)
    private String deviceManufacturer;

    @Column(name = "device_os")
    @Size(max = 50)
    private String deviceOs; // iOS, Android, etc.

    @Column(name = "device_os_version")
    @Size(max = 50)
    private String deviceOsVersion;

    @Column(name = "device_status")
    @Size(max = 50)
    private String deviceStatus; // ACTIVE, INACTIVE, SUSPENDED, LOST, STOLEN

    // SIM Card Information (for SIM Swap)
    @Column(name = "sim_card_number", unique = true)
    @Size(max = 20)
    private String simCardNumber; // ICCID (Integrated Circuit Card Identifier)

    @Column(name = "sim_card_type")
    @Size(max = 50)
    private String simCardType; // PHYSICAL, eSIM

    @Column(name = "sim_card_status")
    @Size(max = 50)
    private String simCardStatus; // ACTIVE, INACTIVE, SUSPENDED

    // Network Preferences (for Quality of Service on Demand)
    @Column(name = "preferred_network_type")
    @Size(max = 50)
    private String preferredNetworkType; // 5G, 4G, 3G

    @Column(name = "qos_enabled")
    private Boolean qosEnabled = false;

    @Column(name = "qos_profile")
    @Size(max = 50)
    private String qosProfile; // STANDARD, PREMIUM, ULTRA

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : lastName != null ? lastName : username;
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (addressLine1 != null) address.append(addressLine1);
        if (addressLine2 != null) {
            if (address.length() > 0) address.append(", ");
            address.append(addressLine2);
        }
        if (city != null) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        if (state != null) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        if (postalCode != null) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }
        if (country != null) {
            if (address.length() > 0) address.append(", ");
            address.append(country);
        }
        return address.toString();
    }
}
