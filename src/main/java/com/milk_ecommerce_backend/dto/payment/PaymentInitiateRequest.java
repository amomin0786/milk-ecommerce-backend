package com.milk_ecommerce_backend.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentInitiateRequest {

    @NotNull
    private Long orderId;

    /**
     * COD / UPI / CARD
     */
    @NotBlank
    private String method;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}