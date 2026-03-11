package com.milk_ecommerce_backend.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class OrderResponse {

    private Long id;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private Timestamp orderDate;
    private Timestamp updatedAt;

    public OrderResponse() {}

    public OrderResponse(Order o) {
        this.id = o.getId();
        this.totalAmount = o.getTotalAmount();
        this.orderStatus = o.getOrderStatus();
        this.paymentMethod = o.getPaymentMethod();
        this.orderDate = o.getOrderDate();
        this.updatedAt = o.getUpdatedAt();
    }

    public Long getId() { return id; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public Timestamp getOrderDate() { return orderDate; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}