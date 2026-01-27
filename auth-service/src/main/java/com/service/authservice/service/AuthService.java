package com.service.authservice.service;

import com.service.shared.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final OAuth2TokenService oAuth2TokenService;

    @Transactional
    public User register(String username, String email, String password,
                       String firstName, String lastName, java.time.LocalDate dateOfBirth,
                       String phoneNumber, String deviceImei, String deviceModel,
                       String deviceManufacturer, String deviceOs, String deviceOsVersion,
                       String addressLine1, String addressLine2, String city,
                       String state, String country, String postalCode,
                       String idDocumentType, String idDocumentNumber,
                       String simCardNumber, String simCardType) {
        return userService.registerUser(username, email, password,
                firstName, lastName, dateOfBirth,
                phoneNumber, deviceImei, deviceModel,
                deviceManufacturer, deviceOs, deviceOsVersion,
                addressLine1, addressLine2, city,
                state, country, postalCode,
                idDocumentType, idDocumentNumber,
                simCardNumber, simCardType);
    }

    public String login(String username, String password) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!userService.validatePassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return oAuth2TokenService.generateToken(user.getUsername(), user.getEmail());
    }
}
