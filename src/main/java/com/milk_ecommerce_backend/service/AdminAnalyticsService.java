package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.admin.AdminAnalyticsResponse;
import com.milk_ecommerce_backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AdminAnalyticsService {

    private final OrderRepository orderRepository;

    public AdminAnalyticsService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public AdminAnalyticsResponse dashboard() {
        long placed = orderRepository.countByOrderStatus("PENDING") + orderRepository.countByOrderStatus("PLACED");
        long paid = orderRepository.countByOrderStatus("PAID");
        long shipped = orderRepository.countByOrderStatus("SHIPPED");
        long delivered = orderRepository.countByOrderStatus("DELIVERED");
        long cancelled = orderRepository.countByOrderStatus("CANCELLED");

        // if you don't have totalRevenue query, compute safely in DB later
        BigDecimal revenue = orderRepository.totalRevenue();

        return new AdminAnalyticsResponse(placed, paid, shipped, delivered, cancelled, revenue);
    }
}