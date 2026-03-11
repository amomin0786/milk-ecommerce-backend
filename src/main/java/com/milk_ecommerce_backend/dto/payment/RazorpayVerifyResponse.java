package com.milk_ecommerce_backend.dto.payment;

public class RazorpayVerifyResponse {

    private boolean verified;
    private Long paymentId;
    private Long orderId;
    private String paymentStatus;
    private String message;

    public RazorpayVerifyResponse() {}

    public RazorpayVerifyResponse(boolean verified, Long paymentId, Long orderId, String paymentStatus, String message) {
        this.verified = verified;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.message = message;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}