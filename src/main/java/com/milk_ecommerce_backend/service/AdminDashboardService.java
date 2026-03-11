package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.AdminDashboardResponse;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.repository.OrderRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import com.milk_ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminDashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    public AdminDashboardResponse getDashboardData() {

        AdminDashboardResponse res = new AdminDashboardResponse();

        // ✅ Counts
        res.setTotalUsers(userRepository.count());
        res.setTotalSellers(userRepository.countByRoleRoleName("SELLER"));
        res.setTotalProducts(productRepository.countByStatus("ACTIVE"));
        res.setTotalOrders(orderRepository.count());

        // ✅ Pending
        res.setPendingSellers(sellerRepository.countByApprovalStatus("PENDING"));
        res.setPendingOrders(orderRepository.countByOrderStatus("PENDING"));

        // ✅ Revenue (PAID orders)
        List<Order> paidOrders = orderRepository.findByOrderStatus("PAID");

        BigDecimal revenue = BigDecimal.ZERO;
        if (paidOrders != null) {
            for (Order o : paidOrders) {
                if (o != null && o.getTotalAmount() != null) {
                    revenue = revenue.add(o.getTotalAmount());
                }
            }
        }

        // Your DTO probably expects double
        res.setTotalRevenue(revenue.doubleValue());

        // ✅ Daily Sales (Last 7 days)
        res.setDailySales(buildDailySales(paidOrders, 7));

        return res;
    }

    private List<Map<String, Object>> buildDailySales(List<Order> paidOrders, int days) {

        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            map.put(today.minusDays(i), BigDecimal.ZERO);
        }

        if (paidOrders != null) {
            for (Order o : paidOrders) {
                if (o != null && o.getOrderDate() != null && o.getTotalAmount() != null) {
                    LocalDate d = o.getOrderDate().toLocalDateTime().toLocalDate();
                    if (map.containsKey(d)) {
                        map.put(d, map.get(d).add(o.getTotalAmount()));
                    }
                }
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : map.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", e.getKey().toString());
            row.put("amount", e.getValue().doubleValue());
            list.add(row);
        }
        return list;
    }
}