package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    long countByRoleRoleName(String roleName);
    long countByStatus(String status);
}