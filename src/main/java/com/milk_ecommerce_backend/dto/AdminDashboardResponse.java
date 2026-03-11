package com.milk_ecommerce_backend.dto;

import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalSellers;
    private long totalProducts;
    private long totalOrders;

    private long pendingSellers;
    private long pendingOrders;

    private double totalRevenue;

    private List<Map<String, Object>> dailySales; // date + amount

    public AdminDashboardResponse() {}

    // getters & setters
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalSellers() { return totalSellers; }
    public void setTotalSellers(long totalSellers) { this.totalSellers = totalSellers; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getPendingSellers() { return pendingSellers; }
    public void setPendingSellers(long pendingSellers) { this.pendingSellers = pendingSellers; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<Map<String, Object>> getDailySales() { return dailySales; }
    public void setDailySales(List<Map<String, Object>> dailySales) { this.dailySales = dailySales; }
}