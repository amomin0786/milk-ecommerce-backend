package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.repository.OrderRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SellerDashboardService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ✅ SELLER DASHBOARD
    public Map<String, Object> getDashboard(String email) {

        Seller seller = sellerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Map<String, Object> data = new HashMap<>();

        long productCount = productRepository.countBySeller_Id(seller.getId());
        long orderCount = orderRepository.countSellerOrders(seller.getId());

        Double revenue = orderRepository.totalSellerRevenue(seller.getId());
        if (revenue == null) {
            revenue = 0.0;
        }

        List<Product> lowStock = productRepository.findTop5BySeller_IdAndStockLessThanEqual(
                seller.getId(), 5
        );

        List<OrderItem> recentOrders = orderRepository.recentSellerOrders(seller.getId());

        data.put("products", productCount);
        data.put("orders", orderCount);
        data.put("revenue", revenue);
        data.put("lowStock", lowStock);
        data.put("recentOrders", recentOrders);

        return data;
    }

    // ✅ SELLER REPORTS DATA
    public Map<String, Object> getReports(String email, String from, String to) {

        Seller seller = sellerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(toDate.atTime(23, 59, 59));

        long deliveredCount = orderRepository.countSellerOrdersByStatusBetween(
                seller.getId(), "DELIVERED", fromTs, toTs
        );

        long pendingCount = orderRepository.countSellerOrdersByStatusBetween(
                seller.getId(), "PENDING", fromTs, toTs
        );

        long shippedCount = orderRepository.countSellerOrdersByStatusBetween(
                seller.getId(), "SHIPPED", fromTs, toTs
        );

        long cancelledCount = orderRepository.countSellerOrdersByStatusBetween(
                seller.getId(), "CANCELLED", fromTs, toTs
        );

        BigDecimal revenue = orderRepository.sellerRevenueBetween(seller.getId(), fromTs, toTs);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        List<Order> deliveredOrders = orderRepository.findSellerOrdersByStatusBetween(
                seller.getId(), "DELIVERED", fromTs, toTs
        );

        Map<String, Object> data = new HashMap<>();
        data.put("from", from);
        data.put("to", to);
        data.put("revenue", revenue);
        data.put("deliveredCount", deliveredCount);
        data.put("pendingCount", pendingCount);
        data.put("shippedCount", shippedCount);
        data.put("cancelledCount", cancelledCount);
        data.put("orders", deliveredOrders);

        return data;
    }

    // ✅ SELLER REPORT CSV EXPORT
    public ResponseEntity<byte[]> exportReportsCsv(String email, String from, String to) {

        Seller seller = sellerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        Timestamp fromTs = Timestamp.valueOf(fromDate.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(toDate.atTime(23, 59, 59));

        List<Order> deliveredOrders = orderRepository.findSellerOrdersByStatusBetween(
                seller.getId(), "DELIVERED", fromTs, toTs
        );

        BigDecimal revenue = orderRepository.sellerRevenueBetween(seller.getId(), fromTs, toTs);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        StringBuilder csv = new StringBuilder();

        csv.append("\"From\",").append(csvValue(from)).append("\n");
        csv.append("\"To\",").append(csvValue(to)).append("\n");
        csv.append("\"Revenue\",").append(csvValue(revenue)).append("\n");
        csv.append("\"Delivered Orders Count\",").append(csvValue(deliveredOrders.size())).append("\n");
        csv.append("\n");

        csv.append("\"Order ID\",\"Order Date\",\"Total Amount\",\"Status\",\"Payment Method\"\n");

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

        byte[] file = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=seller_report.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(file);
    }

    // ✅ COMMON CSV VALUE FORMATTER
    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }

        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}