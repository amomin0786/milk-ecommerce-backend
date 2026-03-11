package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.auth.ForgotPasswordOtpRequest;
import com.milk_ecommerce_backend.dto.auth.MessageResponse;
import com.milk_ecommerce_backend.dto.auth.ResetPasswordWithOtpRequest;
import com.milk_ecommerce_backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/forgot-password/send-otp")
    public MessageResponse sendForgotPasswordOtp(@RequestBody ForgotPasswordOtpRequest request) {
        return authService.sendForgotPasswordOtp(request);
    }

    @PostMapping("/forgot-password/reset")
    public MessageResponse resetPasswordWithOtp(@RequestBody ResetPasswordWithOtpRequest request) {
        return authService.resetPasswordWithOtp(request);
    }
}