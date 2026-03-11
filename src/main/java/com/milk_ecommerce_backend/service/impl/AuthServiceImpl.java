package com.milk_ecommerce_backend.service.impl;

import com.milk_ecommerce_backend.dto.auth.ForgotPasswordOtpRequest;
import com.milk_ecommerce_backend.dto.auth.MessageResponse;
import com.milk_ecommerce_backend.dto.auth.ResetPasswordWithOtpRequest;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.UserRepository;
import com.milk_ecommerce_backend.service.AuthService;
import com.milk_ecommerce_backend.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public MessageResponse sendForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        String email = request == null ? "" : String.valueOf(request.getEmail()).trim();

        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String otp = generateOtp();
        Timestamp expiry = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

        user.setResetOtp(otp);
        user.setResetOtpExpiry(expiry);
        userRepository.save(user);

        // IMPORTANT SECURITY:
        // OTP only goes to database email, not any extra email
        emailService.sendPasswordResetOtpEmail(user.getEmail(), user.getName(), otp);

        return new MessageResponse("OTP sent successfully to your registered email");
    }

    @Override
    public MessageResponse resetPasswordWithOtp(ResetPasswordWithOtpRequest request) {
        String email = request == null ? "" : String.valueOf(request.getEmail()).trim();
        String otp = request == null ? "" : String.valueOf(request.getOtp()).trim();
        String newPassword = request == null ? "" : String.valueOf(request.getNewPassword()).trim();

        if (email.isBlank()) {
          throw new RuntimeException("Email is required");
        }

        if (otp.isBlank()) {
            throw new RuntimeException("OTP is required");
        }

        if (newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }

        if (!isStrongPassword(newPassword)) {
            throw new RuntimeException("Password must contain uppercase, lowercase, number, special character and minimum 8 characters");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        if (user.getResetOtp() == null || user.getResetOtp().isBlank()) {
            throw new RuntimeException("OTP not generated. Please request OTP first");
        }

        if (!otp.equals(user.getResetOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getResetOtpExpiry() == null || user.getResetOtpExpiry().before(new Timestamp(System.currentTimeMillis()))) {
            throw new RuntimeException("OTP expired. Please request a new OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);

        return new MessageResponse("Password reset successful");
    }

    private String generateOtp() {
        Random random = new Random();
        int value = 100000 + random.nextInt(900000);
        return String.valueOf(value);
    }

    private boolean isStrongPassword(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }
}