package com.milk_ecommerce_backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB: user_id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // DB: shop_name
    @Column(name = "shop_name", length = 150, nullable = false)
    private String shopName;

    // DB: gst_number
    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    // DB: approval_status
    @Column(name = "approval_status", length = 20, nullable = false)
    private String approvalStatus = "PENDING"; // PENDING/APPROVED/REJECTED

    // DB: total_sales
    @Column(name = "total_sales", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalSales = BigDecimal.ZERO;

    // DB: approved_date
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    // DB: created_at
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // DB: updated_at
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== getters/setters =====
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}