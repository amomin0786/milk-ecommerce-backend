package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.auth.ForgotPasswordOtpRequest;
import com.milk_ecommerce_backend.dto.auth.MessageResponse;
import com.milk_ecommerce_backend.dto.auth.ResetPasswordWithOtpRequest;

public interface AuthService {

    MessageResponse sendForgotPasswordOtp(ForgotPasswordOtpRequest request);

    MessageResponse resetPasswordWithOtp(ResetPasswordWithOtpRequest request);
}