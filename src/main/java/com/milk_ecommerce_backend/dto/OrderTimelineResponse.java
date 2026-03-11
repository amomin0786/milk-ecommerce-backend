package com.milk_ecommerce_backend.dto;

import com.milk_ecommerce_backend.model.OrderStatusHistory;

public class OrderTimelineResponse {

    public Long id;
    public String status;
    public String note;
    public String updatedByEmail;
    public String createdAt;

    public OrderTimelineResponse(OrderStatusHistory h){
        this.id = h.getId();
        this.status = h.getStatus();
        this.note = h.getNote();
        this.updatedByEmail = h.getUpdatedByEmail();
        this.createdAt = String.valueOf(h.getCreatedAt());
    }
}