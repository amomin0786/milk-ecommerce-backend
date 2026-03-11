package com.milk_ecommerce_backend.mapper;

import com.milk_ecommerce_backend.dto.order.OrderItemResponse;
import com.milk_ecommerce_backend.dto.order.OrderResponse;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Component
public class OrderMapper {

    private Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(String.valueOf(v));
    }

    public OrderResponse toResponse(Order order, List<OrderItem> items) {
        BigDecimal total = toBigDecimal(order.getTotalAmount());

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getOrderStatus(),
                order.getPaymentMethod(),
                total,
                toInstant(order.getOrderDate()),
                toInstant(order.getPaidDate()),
                toInstant(order.getShippedDate()),
                toInstant(order.getDeliveryDate()),
                toInstant(order.getCancelledDate()),
                order.getCancelReason(),
                order.getRefundStatus(),
                toBigDecimal(order.getRefundAmount()),
                order.getRefundReason(),
                toInstant(order.getRefundedDate()),
                items.stream().map(this::toItem).toList()
        );
    }

    private OrderItemResponse toItem(OrderItem item) {
        BigDecimal price = toBigDecimal(item.getPrice());
        int qty = item.getQuantity();
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
        String name = item.getProduct() != null ? item.getProduct().getName() : null;
        Long sellerId = (item.getProduct() != null && item.getProduct().getSeller() != null)
                ? item.getProduct().getSeller().getId()
                : null;

        return new OrderItemResponse(productId, name, sellerId, qty, price, lineTotal);
    }
}