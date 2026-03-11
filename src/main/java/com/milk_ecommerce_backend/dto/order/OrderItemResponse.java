package com.milk_ecommerce_backend.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        Long sellerId,
        int quantity,
        BigDecimal price,
        BigDecimal lineTotal
) {}