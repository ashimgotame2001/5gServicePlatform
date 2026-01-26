package com.service.authservice.service;

import com.service.authservice.entity.User;
import com.service.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String username, String email, String password,
                           String firstName, String lastName, LocalDate dateOfBirth,
                           String phoneNumber, String deviceImei, String deviceModel,
                           String deviceManufacturer, String deviceOs, String deviceOsVersion,
                           String addressLine1, String addressLine2, String city,
                           String state, String country, String postalCode,
                           String idDocumentType, String idDocumentNumber,
                           String simCardNumber, String simCardType) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("Phone number already exists");
        }
        if (deviceImei != null && userRepository.existsByDeviceImei(deviceImei)) {
            throw new RuntimeException("Device IMEI already exists");
        }
        if (simCardNumber != null && userRepository.existsBySimCardNumber(simCardNumber)) {
            throw new RuntimeException("SIM card number already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        // KYC Information
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDateOfBirth(dateOfBirth);
        user.setIdDocumentType(idDocumentType);
        user.setIdDocumentNumber(idDocumentNumber);
        
        // Location Information
        user.setAddressLine1(addressLine1);
        user.setAddressLine2(addressLine2);
        user.setCity(city);
        user.setState(state);
        user.setCountry(country);
        user.setPostalCode(postalCode);
        
        // Phone Number
        user.setPhoneNumber(phoneNumber);
        
        // Device Information
        user.setDeviceImei(deviceImei);
        user.setDeviceModel(deviceModel);
        user.setDeviceManufacturer(deviceManufacturer);
        user.setDeviceOs(deviceOs);
        user.setDeviceOsVersion(deviceOsVersion);
        user.setDeviceStatus("ACTIVE");
        
        // SIM Card Information
        user.setSimCardNumber(simCardNumber);
        user.setSimCardType(simCardType);
        user.setSimCardStatus("ACTIVE");

        return userRepository.save(user);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public Optional<User> findByDeviceImei(String deviceImei) {
        return userRepository.findByDeviceImei(deviceImei);
    }

    public Optional<User> findBySimCardNumber(String simCardNumber) {
        return userRepository.findBySimCardNumber(simCardNumber);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
