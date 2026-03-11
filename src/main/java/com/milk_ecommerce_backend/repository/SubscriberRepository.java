package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    boolean existsByEmail(String email);
}