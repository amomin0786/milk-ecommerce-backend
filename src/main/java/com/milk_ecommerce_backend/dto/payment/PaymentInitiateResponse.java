package com.milk_ecommerce_backend.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentInitiateResponse {

    private Long paymentId;
    private Long orderId;

    private String paymentStatus;   // INITIATED / PENDING / SUCCESS / FAILED
    private String paymentMethod;   // COD / RAZORPAY
    private String transactionId;

    private BigDecimal amount;
    private String currency;

    private String provider; // COD / RAZORPAY
    private LocalDateTime createdAt;

    // Razorpay-specific fields
    private String keyId;
    private String providerOrderId;

    public PaymentInitiateResponse() {}

    public PaymentInitiateResponse(
            Long paymentId,
            Long orderId,
            String paymentStatus,
            String paymentMethod,
            String transactionId,
            BigDecimal amount,
            String currency,
            String provider,
            LocalDateTime createdAt
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }
}