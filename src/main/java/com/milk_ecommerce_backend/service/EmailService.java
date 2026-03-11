package com.milk_ecommerce_backend.service;

public interface EmailService {

	void sendWelcomeEmail(String toEmail, String name);

    void sendOrderPlacedEmail(String toEmail, String name, Long orderId);

    void sendOrderCancelledEmail(String toEmail, String name, Long orderId);

    void sendSellerApprovedEmail(String toEmail, String name, String shopName);

    void sendSellerRejectedEmail(String toEmail, String name, String shopName);

    void sendPasswordResetOtpEmail(String toEmail, String name, String otp);
}