package com.milk_ecommerce_backend.dto.seller;

import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;
import com.milk_ecommerce_backend.model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SellerOrderResponse {

    private Long id;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentMethod;
    private String refundStatus;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingAddress;

    private UserSummary user;
    private List<SellerOrderItemDto> items = new ArrayList<>();

    public SellerOrderResponse() {
    }

    public SellerOrderResponse(Order order) {
        if (order != null) {
            this.id = order.getId();
            this.orderDate = order.getOrderDate();
            this.totalAmount = order.getTotalAmount();
            this.orderStatus = order.getOrderStatus();
            this.paymentMethod = order.getPaymentMethod();
            this.refundStatus = order.getRefundStatus();

            this.customerName = order.getCustomerName();
            this.customerEmail = order.getCustomerEmail();
            this.customerPhone = order.getCustomerPhone();
            this.shippingAddress = order.getShippingAddress();

            User orderUser = order.getUser();
            if (orderUser != null) {
                this.user = new UserSummary(orderUser.getId(), orderUser.getName(), orderUser.getEmail());
            }
        }
    }

    public void addItem(OrderItem item) {
        if (item == null) return;
        this.items.add(new SellerOrderItemDto(item));
    }

    public Long getId() { return id; }
    public Timestamp getOrderDate() { return orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getRefundStatus() { return refundStatus; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public UserSummary getUser() { return user; }
    public List<SellerOrderItemDto> getItems() { return items; }

    public void setId(Long id) { this.id = id; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setUser(UserSummary user) { this.user = user; }
    public void setItems(List<SellerOrderItemDto> items) { this.items = items; }

    public static class UserSummary {
        private Long id;
        private String name;
        private String email;

        public UserSummary() {
        }

        public UserSummary(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class SellerOrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public SellerOrderItemDto() {
        }

        public SellerOrderItemDto(OrderItem item) {
            this.id = item.getId();
            this.quantity = item.getQuantity();
            this.price = item.getPrice();

            if (item.getProduct() != null) {
                this.productId = item.getProduct().getId();
                this.productName = item.getProduct().getName();
            }

            BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
        }

        public Long getId() { return id; }
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getPrice() { return price; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getSubtotal() { return subtotal; }

        public void setId(Long id) { this.id = id; }
        public void setProductId(Long productId) { this.productId = productId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}