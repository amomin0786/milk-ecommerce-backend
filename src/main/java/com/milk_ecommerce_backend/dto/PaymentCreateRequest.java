package com.milk_ecommerce_backend.dto;

import java.math.BigDecimal;

public class PaymentCreateRequest {
    private BigDecimal amount;
    private String method; // UPI / CARD
    private Long orderId;  // optional (we can keep null in our flow)

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}