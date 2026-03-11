package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.ChangePasswordRequest;
import com.milk_ecommerce_backend.dto.UpdateProfileRequest;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.RoleRepository;
import com.milk_ecommerce_backend.repository.UserRepository;
import com.milk_ecommerce_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;

    public User registerUser(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String cleanEmail = user.getEmail().trim().toLowerCase();
        user.setEmail(cleanEmail);

        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(
                    roleRepository.findByRoleName("USER")
                            .orElseThrow(() -> new RuntimeException("Role USER not found in roles table"))
            );
        }

        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("ACTIVE");
        }

        User savedUser = userRepository.save(user);

        try {
            String displayName = savedUser.getName() != null && !savedUser.getName().isBlank()
                    ? savedUser.getName().trim()
                    : "User";

            emailService.sendWelcomeEmail(savedUser.getEmail(), displayName);
        } catch (Exception ex) {
            System.err.println("Welcome email failed: " + ex.getMessage());
        }

        return savedUser;
    }

    public String loginUser(String email, String password) {

        String cleanEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != null && user.getStatus().equalsIgnoreCase("INACTIVE")) {
            throw new RuntimeException("User is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request == null) {
            throw new RuntimeException("Invalid request");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        user.setName(request.getName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

        return userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new RuntimeException("Current password is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("New password is required");
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            throw new RuntimeException("Confirm password is required");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}