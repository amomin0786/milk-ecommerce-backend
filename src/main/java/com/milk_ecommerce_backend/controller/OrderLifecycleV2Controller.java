package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.api.ApiResponse;
import com.milk_ecommerce_backend.dto.order.*;
import com.milk_ecommerce_backend.mapper.OrderMapper;
import com.milk_ecommerce_backend.security.SecurityActorService;
import com.milk_ecommerce_backend.service.OrderLifecycleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/orders")
public class OrderLifecycleV2Controller {

    private final OrderLifecycleService lifecycleService;
    private final SecurityActorService actorService;
    private final OrderMapper orderMapper;

    public OrderLifecycleV2Controller(
            OrderLifecycleService lifecycleService,
            SecurityActorService actorService,
            OrderMapper orderMapper
    ) {
        this.lifecycleService = lifecycleService;
        this.actorService = actorService;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER','USER')")
    public ApiResponse<OrderResponse> get(@PathVariable Long orderId) {
        var order = lifecycleService.getOrderOrThrow(orderId);
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Order fetched", orderMapper.toResponse(order, items));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest req
    ) {
        Long actorUserId = actorService.requireUserId();
        var updated = lifecycleService.updateStatus(orderId, req.status(), actorService.roles(), actorUserId);
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Order status updated", orderMapper.toResponse(updated, items));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<OrderResponse> cancel(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancelRequest req
    ) {
        Long userId = actorService.requireUserId();
        var updated = lifecycleService.cancelOrderByUser(orderId, userId, actorService.roles(), req.reason());
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Order cancelled", orderMapper.toResponse(updated, items));
    }

    @PostMapping("/{orderId}/refund/request")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<OrderResponse> requestRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody RefundRequest req
    ) {
        Long userId = actorService.requireUserId();
        var updated = lifecycleService.requestRefund(orderId, userId, actorService.roles(), req);
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Refund requested", orderMapper.toResponse(updated, items));
    }

    @PostMapping("/{orderId}/refund/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> approveRefund(@PathVariable Long orderId) {
        var updated = lifecycleService.approveRefund(orderId);
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Refund approved", orderMapper.toResponse(updated, items));
    }

    @PostMapping("/{orderId}/refund/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> rejectRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancelRequest req
    ) {
        var updated = lifecycleService.rejectRefund(orderId, req.reason());
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Refund rejected", orderMapper.toResponse(updated, items));
    }

    @PostMapping("/{orderId}/refund/refunded")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> markRefunded(@PathVariable Long orderId) {
        var updated = lifecycleService.markRefunded(orderId);
        var items = lifecycleService.getItems(orderId);
        return ApiResponse.ok("Refund marked refunded", orderMapper.toResponse(updated, items));
    }
}