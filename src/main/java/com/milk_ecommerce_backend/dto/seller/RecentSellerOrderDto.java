package com.milk_ecommerce_backend.dto.seller;

import java.math.BigDecimal;

public class RecentSellerOrderDto {

    private Long orderId;
    private String orderDate;
    private Integer quantity;
    private BigDecimal price;
    private String orderStatus;
    private ProductMiniDto product;

    public RecentSellerOrderDto() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public ProductMiniDto getProduct() {
        return product;
    }

    public void setProduct(ProductMiniDto product) {
        this.product = product;
    }

    public static class ProductMiniDto {
        private Long id;
        private String name;

        public ProductMiniDto() {
        }

        public ProductMiniDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}