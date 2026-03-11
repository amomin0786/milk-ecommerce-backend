package com.milk_ecommerce_backend.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderCancelRequest(
        @NotBlank(message = "reason is required")
        @Size(max = 255, message = "reason must be <= 255 characters")
        String reason
) {}