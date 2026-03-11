package com.milk_ecommerce_backend.dto.seller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SellerDashboardResponse {

    private Long sellerId;
    private String shopName;
    private String approvalStatus;

    private int products;
    private int orders;
    private BigDecimal revenue;

    private List<LowStockItemDto> lowStock = new ArrayList<>();
    private List<RecentSellerOrderDto> recentOrders = new ArrayList<>();

    public SellerDashboardResponse() {
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public List<LowStockItemDto> getLowStock() {
        return lowStock;
    }

    public void setLowStock(List<LowStockItemDto> lowStock) {
        this.lowStock = lowStock;
    }

    public List<RecentSellerOrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentSellerOrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }
}