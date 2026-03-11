package com.milk_ecommerce_backend.dto;

import java.math.BigDecimal;

public class PaymentResponse {
    private String paymentId;
    private BigDecimal amount;
    private String method;
    private String status;
    private boolean verified;

    public PaymentResponse() {}

    public PaymentResponse(String paymentId, BigDecimal amount, String method, String status, boolean verified) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.verified = verified;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}