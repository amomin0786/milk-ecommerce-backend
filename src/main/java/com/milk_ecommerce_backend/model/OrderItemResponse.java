package com.milk_ecommerce_backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderItemResponse {

    private Long id;
    private Long orderId;
    private String orderStatus;

    private Long productId;
    private String productName;
    private String imageUrl;

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;

    public OrderItemResponse() {}

    public OrderItemResponse(OrderItem item) {
        this.id = item.getId();

        if (item.getOrder() != null) {
            this.orderId = item.getOrder().getId();
            this.orderStatus = item.getOrder().getOrderStatus();
        }

        if (item.getProduct() != null) {
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
            this.imageUrl = item.getProduct().getImageUrl();
        }

        BigDecimal p = (item.getPrice() != null) ? item.getPrice() : BigDecimal.ZERO;
        this.price = p.setScale(2, RoundingMode.HALF_UP);

        int q = (item.getQuantity() == null || item.getQuantity() < 1) ? 1 : item.getQuantity();
        this.quantity = q;

        this.lineTotal = this.price.multiply(BigDecimal.valueOf(q)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}