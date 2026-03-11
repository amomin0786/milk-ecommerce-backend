package com.milk_ecommerce_backend.dto;

import com.milk_ecommerce_backend.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SellerProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String status;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerShopName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SellerProductResponse() {
    }

    public SellerProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.imageUrl = product.getImageUrl();
        this.status = product.getStatus();
        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.categoryName = product.getCategory() != null ? product.getCategory().getCategoryName() : null;
        this.sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        this.sellerShopName = product.getSeller() != null ? product.getSeller().getShopName() : null;
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerShopName() {
        return sellerShopName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}