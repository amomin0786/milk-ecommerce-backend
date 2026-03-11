package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.PlaceOrderRequest;
import com.milk_ecommerce_backend.dto.seller.SellerOrderResponse;
import com.milk_ecommerce_backend.exception.NotFoundException;
import com.milk_ecommerce_backend.model.*;
import com.milk_ecommerce_backend.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private OrderStatusHistoryRepository historyRepository;
    @Autowired private EmailService emailService;

    @Transactional
    public Order placeOrderByEmail(String email, PlaceOrderRequest body) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return placeOrder(user.getId(), body);
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrdersByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SellerOrderResponse> getSellerOrdersByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Seller seller = sellerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        List<OrderItem> sellerItems = orderItemRepository.findByProductSellerId(seller.getId());

        Map<Long, SellerOrderResponse> grouped = new LinkedHashMap<>();

        for (OrderItem item : sellerItems) {
            if (item == null || item.getOrder() == null || item.getOrder().getId() == null) {
                continue;
            }

            Order order = item.getOrder();
            Long orderId = order.getId();

            SellerOrderResponse response = grouped.get(orderId);
            if (response == null) {
                response = new SellerOrderResponse(order);
                grouped.put(orderId, response);
            }

            response.addItem(item);
        }

        return grouped.values()
                .stream()
                .sorted((a, b) -> {
                    Long aId = a.getId() != null ? a.getId() : 0L;
                    Long bId = b.getId() != null ? b.getId() : 0L;
                    return bId.compareTo(aId);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Order placeOrder(Long userId, PlaceOrderRequest body) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        String paymentMethod = body != null ? body.getPaymentMethod() : null;
        String customerName = firstNonBlank(
                body != null ? body.getCustomerName() : null,
                user.getName()
        );
        String customerPhone = firstNonBlank(
                body != null ? body.getCustomerPhone() : null,
                user.getPhone()
        );
        String shippingAddress = firstNonBlank(
                body != null ? body.getShippingAddress() : null,
                user.getAddress()
        );

        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new RuntimeException("Shipping address is required");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus("PENDING");
        order.setPaymentMethod((paymentMethod == null || paymentMethod.isBlank()) ? "COD" : paymentMethod);
        order.setCustomerName(customerName);
        order.setCustomerEmail(user.getEmail());
        order.setCustomerPhone(customerPhone);
        order.setShippingAddress(shippingAddress);
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        order.setRefundStatus("NONE");

        order = orderRepository.save(order);
        saveTimeline(order, "PENDING", "Order placed", user.getEmail());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : cartItems) {

            if (cart.getProduct() == null || cart.getProduct().getId() == null) {
                throw new RuntimeException("Invalid cart item: product missing");
            }

            Long productId = cart.getProduct().getId();

            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            Integer stock = product.getStock();
            int qty = (cart.getQuantity() == null || cart.getQuantity() < 1) ? 1 : cart.getQuantity();

            if (stock == null || stock <= 0) {
                throw new RuntimeException("Product out of stock: " + product.getName());
            }
            if (stock < qty) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(stock - qty);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            BigDecimal price = (product.getPrice() != null) ? product.getPrice() : BigDecimal.ZERO;

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setPrice(price);

            orderItemRepository.save(item);

            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteAll(cartItems);

        try {
            String displayName = user.getName() != null && !user.getName().isBlank()
                    ? user.getName().trim()
                    : "User";

            emailService.sendOrderPlacedEmail(
                    user.getEmail(),
                    displayName,
                    savedOrder.getId()
            );
        } catch (Exception ex) {
            System.err.println("Order placed email failed: " + ex.getMessage());
        }

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItemsByEmail(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        String role = "";
        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            role = user.getRole().getRoleName().trim().toUpperCase();
        }

        if ("USER".equals(role)) {
            if (order.getUser() == null || order.getUser().getId() == null ||
                    !order.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Not allowed");
            }
        }

        if ("SELLER".equals(role)) {
            Seller seller = sellerRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new NotFoundException("Seller not found"));

            boolean hasMyItem = orderItemRepository.findByOrder_Id(orderId).stream()
                    .anyMatch(i -> i.getProduct() != null
                            && i.getProduct().getSeller() != null
                            && seller.getId().equals(i.getProduct().getSeller().getId()));

            if (!hasMyItem) {
                throw new RuntimeException("Not allowed");
            }
        }

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);

        return items.stream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Order cancelOrderByEmail(String email, Long orderId, String reason) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getUser() == null || order.getUser().getId() == null ||
                !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not allowed");
        }

        if (!"PENDING".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Order cannot be cancelled now");
        }

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        for (OrderItem it : items) {
            if (it.getProduct() == null || it.getProduct().getId() == null) {
                continue;
            }

            Product p = productRepository.findByIdForUpdate(it.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            int addQty = (it.getQuantity() == null) ? 0 : it.getQuantity();
            int current = (p.getStock() == null) ? 0 : p.getStock();
            p.setStock(current + addQty);
            p.setUpdatedAt(LocalDateTime.now());
            productRepository.save(p);
        }

        saveTimeline(order, "CANCELLED", reason, email);

        order.setOrderStatus("CANCELLED");
        order.setCancelledDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setCancelReason((reason == null || reason.isBlank()) ? "User cancelled" : reason.trim());
        order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        Order savedOrder = orderRepository.save(order);

        try {
            String displayName = user.getName() != null && !user.getName().isBlank()
                    ? user.getName().trim()
                    : "User";

            emailService.sendOrderCancelledEmail(
                    user.getEmail(),
                    displayName,
                    savedOrder.getId()
            );
        } catch (Exception ex) {
            System.err.println("Order cancelled email failed: " + ex.getMessage());
        }

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(String email, Long orderId, String status) {

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status required");
        }

        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String role = "";
        if (actor.getRole() != null && actor.getRole().getRoleName() != null) {
            role = actor.getRole().getRoleName().trim().toUpperCase();
        }

        if (!(role.equals("ADMIN") || role.equals("SELLER"))) {
            throw new RuntimeException("Not allowed");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (role.equals("SELLER")) {
            Seller seller = sellerRepository.findByUser_Id(actor.getId())
                    .orElseThrow(() -> new NotFoundException("Seller not found"));

            boolean hasMyItem = orderItemRepository.findByOrder_Id(orderId).stream()
                    .anyMatch(i -> i.getProduct() != null
                            && i.getProduct().getSeller() != null
                            && seller.getId().equals(i.getProduct().getSeller().getId()));

            if (!hasMyItem) {
                throw new RuntimeException("Not allowed");
            }
        }

        saveTimeline(order, status, "Status updated", email);
        order.setOrderStatus(status.trim().toUpperCase());
        order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getTimelineByEmail(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        String role = "";
        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            role = user.getRole().getRoleName().trim().toUpperCase();
        }

        if ("USER".equals(role)) {
            if (order.getUser() == null || order.getUser().getId() == null ||
                    !order.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Not allowed");
            }
        }

        if ("SELLER".equals(role)) {
            Seller seller = sellerRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new NotFoundException("Seller not found"));

            boolean hasMyItem = orderItemRepository.findByOrder_Id(orderId).stream()
                    .anyMatch(i -> i.getProduct() != null
                            && i.getProduct().getSeller() != null
                            && seller.getId().equals(i.getProduct().getSeller().getId()));

            if (!hasMyItem) {
                throw new RuntimeException("Not allowed");
            }
        }

        return historyRepository.findByOrder_IdOrderByCreatedAtAsc(orderId);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportAdminOrdersCsv(String from, String to) {

        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new RuntimeException("From date and To date are required");
        }

        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date cannot be greater than To date");
        }

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(toDate.atTime(23, 59, 59));

        List<Order> orders = orderRepository.findByOrderDateBetweenOrderByOrderDateDesc(fromTs, toTs);

        StringBuilder csv = new StringBuilder();

        csv.append("\"From\",").append(csvValue(from)).append("\n");
        csv.append("\"To\",").append(csvValue(to)).append("\n");
        csv.append("\"Total Orders\",").append(csvValue(orders.size())).append("\n");
        csv.append("\n");

        csv.append("\"Order ID\",\"Customer Name\",\"Customer Email\",\"Customer Phone\",\"Shipping Address\",\"Order Date\",\"Total Amount\",\"Status\",\"Payment Method\"\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Order o : orders) {
            String orderId = o.getId() != null ? String.valueOf(o.getId()) : "";
            String customerName = o.getCustomerName() != null ? o.getCustomerName() : "";
            String customerEmail = o.getCustomerEmail() != null ? o.getCustomerEmail() : "";
            String customerPhone = o.getCustomerPhone() != null ? o.getCustomerPhone() : "";
            String shippingAddress = o.getShippingAddress() != null ? o.getShippingAddress() : "";
            String orderDate = o.getOrderDate() != null ? sdf.format(o.getOrderDate()) : "";
            String totalAmount = o.getTotalAmount() != null ? o.getTotalAmount().toString() : "";
            String status = o.getOrderStatus() != null ? o.getOrderStatus() : "";
            String paymentMethod = o.getPaymentMethod() != null ? o.getPaymentMethod() : "";

            csv.append(csvValue(orderId)).append(",");
            csv.append(csvValue(customerName)).append(",");
            csv.append(csvValue(customerEmail)).append(",");
            csv.append(csvValue(customerPhone)).append(",");
            csv.append(csvValue(shippingAddress)).append(",");
            csv.append(csvValue(orderDate)).append(",");
            csv.append(csvValue(totalAmount)).append(",");
            csv.append(csvValue(status)).append(",");
            csv.append(csvValue(paymentMethod)).append("\n");
        }

        byte[] file = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=admin_orders_report.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(file);
    }

    private void saveTimeline(Order order, String status, String note, String email) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setStatus(status);
        h.setNote(note);
        h.setUpdatedByEmail(email);
        historyRepository.save(h);
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first.trim();
        if (second != null && !second.isBlank()) return second.trim();
        return "";
    }
}