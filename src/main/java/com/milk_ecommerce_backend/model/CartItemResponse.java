package com.milk_ecommerce_backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;

    public CartItemResponse() {}

    public CartItemResponse(Cart c) {
        this.id = c.getId();

        this.quantity = (c.getQuantity() == null || c.getQuantity() < 1) ? 1 : c.getQuantity();

        if (c.getProduct() != null) {
            this.productId = c.getProduct().getId();
            this.productName = c.getProduct().getName();
            this.imageUrl = c.getProduct().getImageUrl();

            BigDecimal p = (c.getProduct().getPrice() != null) ? c.getProduct().getPrice() : BigDecimal.ZERO;
            this.price = p.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        this.lineTotal = this.price.multiply(BigDecimal.valueOf(this.quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}