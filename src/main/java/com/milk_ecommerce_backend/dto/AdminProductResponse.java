package com.milk_ecommerce_backend.dto;

import java.math.BigDecimal;

public class AdminProductResponse {

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
    private String sellerUserName;
    private String sellerUserEmail;

    public AdminProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            String status,
            Long categoryId,
            String categoryName,
            Long sellerId,
            String sellerShopName,
            String sellerUserName,
            String sellerUserEmail
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.sellerId = sellerId;
        this.sellerShopName = sellerShopName;
        this.sellerUserName = sellerUserName;
        this.sellerUserEmail = sellerUserEmail;
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

    public String getSellerUserName() {
        return sellerUserName;
    }

    public String getSellerUserEmail() {
        return sellerUserEmail;
    }
}