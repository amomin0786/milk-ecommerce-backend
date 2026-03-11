package com.milk_ecommerce_backend.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundRequest(
        @Positive(message = "amount must be > 0")
        BigDecimal amount,

        @NotBlank(message = "reason is required")
        @Size(max = 255, message = "reason must be <= 255 characters")
        String reason
) {}