package com.milk_ecommerce_backend.dto.order;

import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "status is required")
        OrderStatusDto status
) {}