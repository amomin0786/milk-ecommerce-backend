package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.PlaceOrderRequest;
import com.milk_ecommerce_backend.dto.seller.SellerOrderResponse;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItemResponse;
import com.milk_ecommerce_backend.model.OrderStatusHistory;
import com.milk_ecommerce_backend.service.InvoiceService;
import com.milk_ecommerce_backend.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/place")
    public Order place(Authentication auth, @RequestBody(required = false) PlaceOrderRequest body) {
        String email = auth.getName();

        PlaceOrderRequest req = (body == null) ? new PlaceOrderRequest() : body;

        if (req.getPaymentMethod() == null || req.getPaymentMethod().isBlank()) {
            req.setPaymentMethod("COD");
        }

        return orderService.placeOrderByEmail(email, req);
    }

    @GetMapping("/my")
    public List<Order> my(Authentication auth) {
        return orderService.getUserOrdersByEmail(auth.getName());
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItemResponse> items(Authentication auth,
                                         @PathVariable Long orderId) {
        return orderService.getOrderItemsByEmail(auth.getName(), orderId);
    }

    @PutMapping("/{orderId}/cancel")
    public Order cancel(Authentication auth,
                        @PathVariable Long orderId,
                        @RequestParam(required = false) String reason) {
        return orderService.cancelOrderByEmail(auth.getName(), orderId, reason);
    }

    @GetMapping("/admin/all")
    public List<Order> allForAdmin() {
        return orderService.getAllOrders();
    }

    @GetMapping("/admin/export")
    public ResponseEntity<byte[]> exportAdminOrders(
            @RequestParam String from,
            @RequestParam String to) {
        return orderService.exportAdminOrdersCsv(from, to);
    }

    @PutMapping("/{orderId}/status")
    public Order updateStatus(Authentication auth,
                              @PathVariable Long orderId,
                              @RequestParam String status) {
        return orderService.updateOrderStatus(auth.getName(), orderId, status);
    }

    @GetMapping("/seller/items")
    public List<SellerOrderResponse> sellerItems(Authentication auth) {
        return orderService.getSellerOrdersByEmail(auth.getName());
    }

    @GetMapping("/{orderId}/timeline")
    public List<OrderStatusHistory> timeline(Authentication auth,
                                             @PathVariable Long orderId) {
        return orderService.getTimelineByEmail(auth.getName(), orderId);
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> invoice(@PathVariable Long orderId) {

        byte[] pdf = invoiceService.generateInvoice(orderId);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=invoice_" + orderId + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}