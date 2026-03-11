package com.milk_ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class SellerApplyRequest {

    @NotBlank(message = "shopName is required")
    private String shopName;

    private String gstNumber;

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
}