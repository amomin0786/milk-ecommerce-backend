package com.milk_ecommerce_backend.service.impl;

import com.milk_ecommerce_backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        String safeName = (name == null || name.isBlank()) ? "User" : name.trim();

        String subject = "Welcome to Milk Marketplace";
        String body = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Welcome to Milk Marketplace</h2>
                    <p>Hello %s,</p>
                    <p>Your account has been created successfully.</p>
                    <p>You can now log in, browse products, add to cart, and place orders.</p>
                    <p>Thank you for joining us.</p>
                  </body>
                </html>
                """.formatted(safeName);

        sendHtmlEmail(toEmail, subject, body);
    }

    @Override
    public void sendOrderPlacedEmail(String toEmail, String customerName, Long orderId) {
        String safeName = (customerName == null || customerName.isBlank()) ? "User" : customerName.trim();
        Long safeOrderId = (orderId == null) ? 0L : orderId;

        String subject = "Order Placed Successfully";
        String body = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Order Confirmation</h2>
                    <p>Hello %s,</p>
                    <p>Your order has been placed successfully.</p>
                    <p><strong>Order ID:</strong> %d</p>
                    <p>We will update you when your order status changes.</p>
                    <p>Thank you for shopping with Milk Marketplace.</p>
                  </body>
                </html>
                """.formatted(safeName, safeOrderId);

        sendHtmlEmail(toEmail, subject, body);
    }

    @Override
    public void sendOrderCancelledEmail(String toEmail, String customerName, Long orderId) {
        String safeName = (customerName == null || customerName.isBlank()) ? "User" : customerName.trim();
        Long safeOrderId = (orderId == null) ? 0L : orderId;

        String subject = "Order Cancelled";
        String body = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Order Cancelled</h2>
                    <p>Hello %s,</p>
                    <p>Your order has been cancelled successfully.</p>
                    <p><strong>Order ID:</strong> %d</p>
                  </body>
                </html>
                """.formatted(safeName, safeOrderId);

        sendHtmlEmail(toEmail, subject, body);
    }

    @Override
    public void sendSellerApprovedEmail(String toEmail, String name, String shopName) {
        String safeName = (name == null || name.isBlank()) ? "User" : name.trim();
        String safeShopName = (shopName == null || shopName.isBlank()) ? "-" : shopName.trim();

        String subject = "Seller Request Approved";
        String body = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Seller Request Approved</h2>
                    <p>Hello %s,</p>
                    <p>Your seller request for shop <strong>%s</strong> has been approved.</p>
                    <p>You can now access the seller dashboard and manage your products.</p>
                  </body>
                </html>
                """.formatted(safeName, safeShopName);

        sendHtmlEmail(toEmail, subject, body);
    }

    @Override
    public void sendSellerRejectedEmail(String toEmail, String name, String shopName) {
        String safeName = (name == null || name.isBlank()) ? "User" : name.trim();
        String safeShopName = (shopName == null || shopName.isBlank()) ? "-" : shopName.trim();

        String subject = "Seller Request Rejected";
        String body = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Seller Request Rejected</h2>
                    <p>Hello %s,</p>
                    <p>Your seller request for shop <strong>%s</strong> has been rejected.</p>
                    <p>Please contact admin if you need more details.</p>
                  </body>
                </html>
                """.formatted(safeName, safeShopName);

        sendHtmlEmail(toEmail, subject, body);
    }

    @Override
    public void sendPasswordResetOtpEmail(String toEmail, String name, String otp) {
        String safeName = (name == null || name.isBlank()) ? "User" : name.trim();
        String safeOtp = (otp == null || otp.isBlank()) ? "------" : otp.trim();

        String subject = "Password Reset OTP";
        String body = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Password Reset Request</h2>
                    <p>Hello %s,</p>
                    <p>Your OTP for password reset is:</p>
                    <div style="font-size: 30px; font-weight: bold; letter-spacing: 6px; margin: 16px 0;">%s</div>
                    <p>This OTP is valid for <strong>10 minutes</strong>.</p>
                    <p>If you did not request this, please ignore this email.</p>
                  </body>
                </html>
                """.formatted(safeName, safeOtp);

        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String toEmail, String subject, String body) {
        if (!mailEnabled) {
            System.out.println("Mail sending is disabled.");
            return;
        }

        if (toEmail == null || toEmail.isBlank()) {
            System.out.println("Recipient email is empty. Mail not sent.");
            return;
        }

        try {
            String cleanToEmail = toEmail.trim();

            System.out.println("Sending email to: " + cleanToEmail + " | Subject: " + subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(cleanToEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);

            System.out.println("Email sent successfully to: " + cleanToEmail);
        } catch (MailException | MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}