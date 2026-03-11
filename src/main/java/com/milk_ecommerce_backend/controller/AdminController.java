package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.OrderRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import com.milk_ecommerce_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block/{id}")
    public String blockUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("BLOCKED");
        userRepository.save(user);
        return "User blocked successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/unblock/{id}")
    public String unblockUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("ACTIVE");
        userRepository.save(user);
        return "User unblocked successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();

        BigDecimal revenue = orderRepository.totalRevenue();
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        stats.put("users", userRepository.count());
        stats.put("sellers", sellerRepository.count());
        stats.put("products", productRepository.count());
        stats.put("orders", orderRepository.count());

        stats.put("revenue", revenue);
        stats.put("pendingOrders", orderRepository.countByOrderStatus("PENDING"));
        stats.put("confirmedOrders", orderRepository.countByOrderStatus("CONFIRMED"));
        stats.put("shippedOrders", orderRepository.countByOrderStatus("SHIPPED"));
        stats.put("deliveredOrders", orderRepository.countByOrderStatus("DELIVERED"));
        stats.put("cancelledOrders", orderRepository.countByOrderStatus("CANCELLED"));

        return stats;
    }

    // ✅ REPORTS DATA
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public Map<String, Object> getReports(
            @RequestParam String from,
            @RequestParam String to
    ) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(toDate.atTime(23, 59, 59));

        List<Order> deliveredOrders =
                orderRepository.findByOrderStatusAndOrderDateBetween("DELIVERED", fromTs, toTs);

        List<Order> pendingOrders =
                orderRepository.findByOrderStatusAndOrderDateBetween("PENDING", fromTs, toTs);

        List<Order> shippedOrders =
                orderRepository.findByOrderStatusAndOrderDateBetween("SHIPPED", fromTs, toTs);

        List<Order> cancelledOrders =
                orderRepository.findByOrderStatusAndOrderDateBetween("CANCELLED", fromTs, toTs);

        BigDecimal revenue = orderRepository.revenueBetween(fromTs, toTs);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("from", from);
        data.put("to", to);
        data.put("revenue", revenue);
        data.put("deliveredCount", deliveredOrders.size());
        data.put("pendingCount", pendingOrders.size());
        data.put("shippedCount", shippedOrders.size());
        data.put("cancelledCount", cancelledOrders.size());
        data.put("orders", deliveredOrders);

        return data;
    }

    // ✅ REPORTS EXPORT CSV
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportReportsCsv(
            @RequestParam String from,
            @RequestParam String to
    ) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(toDate.atTime(23, 59, 59));

        List<Order> deliveredOrders =
                orderRepository.findByOrderStatusAndOrderDateBetween("DELIVERED", fromTs, toTs);

        BigDecimal revenue = orderRepository.revenueBetween(fromTs, toTs);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        StringBuilder csv = new StringBuilder();

        csv.append("From,").append(csvValue(from)).append("\n");
        csv.append("To,").append(csvValue(to)).append("\n");
        csv.append("Revenue,").append(csvValue(revenue)).append("\n");
        csv.append("Delivered Orders Count,").append(csvValue(deliveredOrders.size())).append("\n");
        csv.append("\n");

        csv.append("Order ID,Order Date,Total Amount,Status,Payment Method\n");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Order o : deliveredOrders) {
            String orderId = o.getId() != null ? String.valueOf(o.getId()) : "";
            String orderDate = o.getOrderDate() != null ? sdf.format(o.getOrderDate()) : "";
            String totalAmount = o.getTotalAmount() != null ? o.getTotalAmount().toString() : "";
            String status = o.getOrderStatus() != null ? o.getOrderStatus() : "";
            String paymentMethod = o.getPaymentMethod() != null ? o.getPaymentMethod() : "";

            csv.append(csvValue(orderId)).append(",");
            csv.append(csvValue(orderDate)).append(",");
            csv.append(csvValue(totalAmount)).append(",");
            csv.append(csvValue(status)).append(",");
            csv.append(csvValue(paymentMethod)).append("\n");
        }

        byte[] file = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=admin_report.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(file);
    }

    // ✅ ALL ORDERS EXPORT CSV
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportAllOrdersCsv() {

        List<Order> orders = orderRepository.findAll();
        orders.sort((a, b) -> {
            if (a.getId() == null && b.getId() == null) return 0;
            if (a.getId() == null) return 1;
            if (b.getId() == null) return -1;
            return Long.compare(b.getId(), a.getId());
        });

        StringBuilder csv = new StringBuilder();

        csv.append("\"Order ID\",\"Order Date\",\"Total Amount\",\"Status\",\"Payment Method\",\"User Email\"\n");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Order o : orders) {
            String orderId = o.getId() != null ? String.valueOf(o.getId()) : "";
            String orderDate = o.getOrderDate() != null ? sdf.format(o.getOrderDate()) : "";
            String totalAmount = o.getTotalAmount() != null ? o.getTotalAmount().toString() : "";
            String status = o.getOrderStatus() != null ? o.getOrderStatus() : "";
            String paymentMethod = o.getPaymentMethod() != null ? o.getPaymentMethod() : "";
            String userEmail = (o.getUser() != null && o.getUser().getEmail() != null)
                    ? o.getUser().getEmail()
                    : "";

            csv.append(csvValue(orderId)).append(",");
            csv.append(csvValue(orderDate)).append(",");
            csv.append(csvValue(totalAmount)).append(",");
            csv.append(csvValue(status)).append(",");
            csv.append(csvValue(paymentMethod)).append(",");
            csv.append(csvValue(userEmail)).append("\n");
        }

        byte[] file = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=admin_orders.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(file);
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }

        String s = String.valueOf(value);
        s = s.replace("\"", "\"\"");

        return "\"" + s + "\"";
    }
}