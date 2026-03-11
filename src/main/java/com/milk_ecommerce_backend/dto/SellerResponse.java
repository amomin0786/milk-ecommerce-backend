package com.milk_ecommerce_backend.dto;

import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SellerResponse {

    private Long id;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userAddress;
    private String userStatus;
    private String shopName;
    private String gstNumber;
    private String approvalStatus;
    private BigDecimal totalSales;
    private LocalDateTime createdAt;
    private LocalDateTime approvedDate;

    public SellerResponse(Seller s) {
        User u = s.getUser();

        this.id = s.getId();
        this.userName = u != null ? u.getName() : null;
        this.userEmail = u != null ? u.getEmail() : null;
        this.userPhone = u != null ? u.getPhone() : null;
        this.userAddress = u != null ? u.getAddress() : null;
        this.userStatus = u != null ? u.getStatus() : null;
        this.shopName = s.getShopName();
        this.gstNumber = s.getGstNumber();
        this.approvalStatus = s.getApprovalStatus();
        this.totalSales = s.getTotalSales();
        this.createdAt = s.getCreatedAt();
        this.approvedDate = s.getApprovedDate();
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public String getShopName() {
        return shopName;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }
}