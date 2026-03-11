package com.milk_ecommerce_backend.dto.auth;

public class ForgotPasswordOtpRequest {

    private String email;

    public ForgotPasswordOtpRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}