package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);
//    Optional<Role> findByRoleName(String roleName);
    

}