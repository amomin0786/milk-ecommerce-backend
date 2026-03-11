package com.milk_ecommerce_backend.dto.admin;

import java.math.BigDecimal;

public class AdminAnalyticsResponse {

    private long placedOrders;
    private long paidOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;

    public AdminAnalyticsResponse() {}

    public AdminAnalyticsResponse(long placedOrders, long paidOrders, long shippedOrders,
                                  long deliveredOrders, long cancelledOrders, BigDecimal totalRevenue) {
        this.placedOrders = placedOrders;
        this.paidOrders = paidOrders;
        this.shippedOrders = shippedOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalRevenue = totalRevenue;
    }

    public long getPlacedOrders() { return placedOrders; }
    public void setPlacedOrders(long placedOrders) { this.placedOrders = placedOrders; }

    public long getPaidOrders() { return paidOrders; }
    public void setPaidOrders(long paidOrders) { this.paidOrders = paidOrders; }

    public long getShippedOrders() { return shippedOrders; }
    public void setShippedOrders(long shippedOrders) { this.shippedOrders = shippedOrders; }

    public long getDeliveredOrders() { return deliveredOrders; }
    public void setDeliveredOrders(long deliveredOrders) { this.deliveredOrders = deliveredOrders; }

    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}