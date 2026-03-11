package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    List<Payment> findByOrder_User_Id(Long userId);
}