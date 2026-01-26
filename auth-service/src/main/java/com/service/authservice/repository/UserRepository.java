package com.service.authservice.repository;

import com.service.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByDeviceImei(String deviceImei);
    Optional<User> findBySimCardNumber(String simCardNumber);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByDeviceImei(String deviceImei);
    boolean existsBySimCardNumber(String simCardNumber);
}
