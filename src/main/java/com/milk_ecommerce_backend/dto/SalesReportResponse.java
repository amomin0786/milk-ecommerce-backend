package com.milk_ecommerce_backend.dto;

import java.util.List;
import java.util.Map;

public class SalesReportResponse {

    private long totalOrders;
    private double totalRevenue;
    private List<Map<String, Object>> dailySales; // date, amount

    public SalesReportResponse() {}

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<Map<String, Object>> getDailySales() { return dailySales; }
    public void setDailySales(List<Map<String, Object>> dailySales) { this.dailySales = dailySales; }
}