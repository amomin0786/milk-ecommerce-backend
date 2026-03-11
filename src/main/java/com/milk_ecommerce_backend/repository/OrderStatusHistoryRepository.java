package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrder_IdOrderByCreatedAtAsc(Long orderId);

}