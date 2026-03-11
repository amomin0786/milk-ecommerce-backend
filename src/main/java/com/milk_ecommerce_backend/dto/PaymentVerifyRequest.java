package com.milk_ecommerce_backend.dto;

public class PaymentVerifyRequest {
    private String paymentId;
    private String status; // SUCCESS / FAILED

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}