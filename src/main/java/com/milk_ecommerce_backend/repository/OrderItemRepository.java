package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);


	List<OrderItem> findByProductSellerId(Long id);
	

	    List<OrderItem> findByProduct_Id(Long productId);
	
}