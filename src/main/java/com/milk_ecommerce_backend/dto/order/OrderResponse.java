package com.milk_ecommerce_backend.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long userId,
        String orderStatus,
        String paymentMethod,
        BigDecimal totalAmount,
        Instant orderDate,
        Instant paidDate,
        Instant shippedDate,
        Instant deliveryDate,
        Instant cancelledDate,
        String cancelReason,
        String refundStatus,
        BigDecimal refundAmount,
        String refundReason,
        Instant refundedDate,
        List<OrderItemResponse> items
) {}