package com.milk_ecommerce_backend.repository;

import com.milk_ecommerce_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStatus(String status);

    List<Category> findByStatusOrStatusIsNullOrderByCategoryNameAsc(String status);
}