package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.audit.Audited;
import com.milk_ecommerce_backend.dto.order.OrderStatusDto;
import com.milk_ecommerce_backend.dto.order.RefundRequest;
import com.milk_ecommerce_backend.exception.BadRequestException;
import com.milk_ecommerce_backend.exception.ConflictException;
import com.milk_ecommerce_backend.exception.ForbiddenException;
import com.milk_ecommerce_backend.exception.NotFoundException;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.repository.OrderItemRepository;
import com.milk_ecommerce_backend.repository.OrderRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class OrderLifecycleService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    public OrderLifecycleService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            SellerRepository sellerRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
    }

    public Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    public List<OrderItem> getItems(Long orderId) {
        return orderItemRepository.findByOrder_Id(orderId);
    }

    private boolean isAdmin(Set<String> roles) {
        return roles.contains("ROLE_ADMIN") || roles.contains("ADMIN");
    }

    private boolean isSeller(Set<String> roles) {
        return roles.contains("ROLE_SELLER") || roles.contains("SELLER");
    }

    private boolean isUser(Set<String> roles) {
        return roles.contains("ROLE_USER") || roles.contains("USER");
    }

    private Seller sellerByUserIdOrThrow(Long userId) {
        return sellerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ForbiddenException("Seller account not found for user: " + userId));
    }

    private boolean orderBelongsToSeller(Long orderId, Long sellerId) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        return items.stream().anyMatch(i ->
                i.getProduct() != null &&
                i.getProduct().getSeller() != null &&
                sellerId.equals(i.getProduct().getSeller().getId())
        );
    }

    private String normalizeStatus(String current) {
        if (current == null || current.isBlank()) return "PLACED";
        if ("PENDING".equalsIgnoreCase(current)) return "PLACED";
        return current.toUpperCase();
    }

    private void validateTransition(String current, String target) {
        if (current.equals(target)) return;

        if ("CANCELLED".equals(current)) throw new ConflictException("Cannot transition from CANCELLED");
        if ("DELIVERED".equals(current)) throw new ConflictException("Cannot transition from DELIVERED");

        boolean allowed =
                (current.equals("PLACED") && (target.equals("PAID") || target.equals("CANCELLED"))) ||
                (current.equals("PAID") && (target.equals("SHIPPED") || target.equals("CANCELLED"))) ||
                (current.equals("SHIPPED") && target.equals("DELIVERED"));

        if (!allowed) throw new ConflictException("Invalid status transition: " + current + " -> " + target);
    }

    @Transactional
    @Audited(action = "ORDER_STATUS_UPDATE", entityType = "Order", entityIdParam = "orderId")
    public Order updateStatus(Long orderId, OrderStatusDto newStatus, Set<String> roles, Long actorUserId) {
        Order order = getOrderOrThrow(orderId);

        String current = normalizeStatus(order.getOrderStatus());
        String target = newStatus.name();

        // Authorization: ADMIN any, SELLER only their order
        if (isAdmin(roles)) {
            // ok
        } else if (isSeller(roles)) {
            Seller seller = sellerByUserIdOrThrow(actorUserId);
            if (!orderBelongsToSeller(orderId, seller.getId())) {
                throw new ForbiddenException("You are not allowed to update this order");
            }
        } else {
            throw new ForbiddenException("You are not allowed to update this order");
        }

        validateTransition(current, target);

        order.setOrderStatus(target);
        Timestamp now = Timestamp.from(Instant.now());

        switch (target) {
            case "PAID" -> order.setPaidDate(now);
            case "SHIPPED" -> order.setShippedDate(now);
            case "DELIVERED" -> order.setDeliveryDate(now);
            case "CANCELLED" -> order.setCancelledDate(now);
        }

        order.setUpdatedAt(now);
        return orderRepository.save(order);
    }

    @Transactional
    @Audited(action = "ORDER_CANCEL", entityType = "Order", entityIdParam = "orderId")
    public Order cancelOrderByUser(Long orderId, Long actorUserId, Set<String> roles, String reason) {
        if (!isUser(roles)) throw new ForbiddenException("Only USER can cancel order");

        Order order = getOrderOrThrow(orderId);
        if (order.getUser() == null || !actorUserId.equals(order.getUser().getId())) {
            throw new ForbiddenException("You are not allowed to cancel this order");
        }

        String status = normalizeStatus(order.getOrderStatus());
        if ("CANCELLED".equals(status)) throw new ConflictException("Order already cancelled");
        if ("SHIPPED".equals(status) || "DELIVERED".equals(status)) {
            throw new ConflictException("Order cannot be cancelled after shipment");
        }

        // restock on cancellation
        restock(orderId);

        Timestamp now = Timestamp.from(Instant.now());
        order.setOrderStatus("CANCELLED");
        order.setCancelledDate(now);
        order.setCancelReason(reason);
        order.setUpdatedAt(now);

        // if already paid -> auto refund request
        if (order.getPaidDate() != null) {
            order.setRefundStatus("REQUESTED");
            order.setRefundAmount(order.getTotalAmount());
            order.setRefundReason("Auto refund due to cancellation: " + reason);
        } else {
            if (order.getRefundStatus() == null) order.setRefundStatus("NONE");
        }

        return orderRepository.save(order);
    }

    @Transactional
    @Audited(action = "REFUND_REQUEST", entityType = "Order", entityIdParam = "orderId")
    public Order requestRefund(Long orderId, Long actorUserId, Set<String> roles, RefundRequest req) {
        if (!isUser(roles)) throw new ForbiddenException("Only USER can request refund");

        Order order = getOrderOrThrow(orderId);
        if (order.getUser() == null || !actorUserId.equals(order.getUser().getId())) {
            throw new ForbiddenException("You are not allowed to request refund");
        }

        if (order.getPaidDate() == null) throw new BadRequestException("Refund can be requested only after payment");

        String status = normalizeStatus(order.getOrderStatus());
        if (!status.equals("CANCELLED") && !status.equals("DELIVERED")) {
            throw new BadRequestException("Refund allowed only for CANCELLED or DELIVERED order");
        }

        String rs = order.getRefundStatus() == null ? "NONE" : order.getRefundStatus().toUpperCase();
        if (!rs.equals("NONE") && !rs.equals("REJECTED")) throw new ConflictException("Refund already requested/processed");

        BigDecimal amount = req.amount() == null ? order.getTotalAmount() : req.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(order.getTotalAmount()) > 0) {
            throw new BadRequestException("Invalid refund amount");
        }

        Timestamp now = Timestamp.from(Instant.now());
        order.setRefundStatus("REQUESTED");
        order.setRefundAmount(amount);
        order.setRefundReason(req.reason());
        order.setUpdatedAt(now);

        return orderRepository.save(order);
    }

    @Transactional
    @Audited(action = "REFUND_APPROVE", entityType = "Order", entityIdParam = "orderId")
    public Order approveRefund(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        String rs = order.getRefundStatus() == null ? "NONE" : order.getRefundStatus().toUpperCase();
        if (!rs.equals("REQUESTED")) throw new ConflictException("Refund is not in REQUESTED state");

        Timestamp now = Timestamp.from(Instant.now());
        order.setRefundStatus("APPROVED");
        order.setUpdatedAt(now);
        return orderRepository.save(order);
    }

    @Transactional
    @Audited(action = "REFUND_REJECT", entityType = "Order", entityIdParam = "orderId")
    public Order rejectRefund(Long orderId, String reason) {
        Order order = getOrderOrThrow(orderId);

        String rs = order.getRefundStatus() == null ? "NONE" : order.getRefundStatus().toUpperCase();
        if (!rs.equals("REQUESTED")) throw new ConflictException("Refund is not in REQUESTED state");

        Timestamp now = Timestamp.from(Instant.now());
        order.setRefundStatus("REJECTED");
        order.setRefundReason(reason);
        order.setUpdatedAt(now);
        return orderRepository.save(order);
    }

    @Transactional
    @Audited(action = "REFUND_MARK_REFUNDED", entityType = "Order", entityIdParam = "orderId")
    public Order markRefunded(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        String rs = order.getRefundStatus() == null ? "NONE" : order.getRefundStatus().toUpperCase();
        if (!rs.equals("APPROVED")) throw new ConflictException("Refund must be APPROVED before marking REFUNDED");

        Timestamp now = Timestamp.from(Instant.now());
        order.setRefundStatus("REFUNDED");
        order.setRefundedDate(now);
        order.setUpdatedAt(now);
        return orderRepository.save(order);
    }

    private void restock(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        List<Long> productIds = items.stream().map(i -> i.getProduct().getId()).distinct().toList();

        var locked = productRepository.findAllByIdForUpdate(productIds);

        for (OrderItem it : items) {
            Long pid = it.getProduct().getId();
            Product p = locked.stream().filter(x -> x.getId().equals(pid)).findFirst()
                    .orElseThrow(() -> new NotFoundException("Product not found for restock: " + pid));

            p.setStock(p.getStock() + it.getQuantity());
            productRepository.save(p);
        }
    }
}