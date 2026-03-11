package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.model.Category;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.repository.CategoryRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // ✅ GET ALL
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ✅ ADD
    public Category addCategory(Category category) {

        if (category == null) {
            throw new RuntimeException("Category data is required");
        }

        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new RuntimeException("Category name is required");
        }

        Category c = new Category();
        c.setCategoryName(category.getCategoryName().trim());
        c.setDescription(category.getDescription() != null ? category.getDescription().trim() : null);

        if (category.getStatus() == null || category.getStatus().isBlank()) {
            c.setStatus("ACTIVE");
        } else {
            c.setStatus(category.getStatus().trim().toUpperCase());
        }

        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(c);
    }

    // ✅ UPDATE
    public Category updateCategory(Long id, Category category) {

        if (id == null) {
            throw new RuntimeException("Category id is required");
        }

        if (category == null) {
            throw new RuntimeException("Category data is required");
        }

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new RuntimeException("Category name is required");
        }

        existing.setCategoryName(category.getCategoryName().trim());
        existing.setDescription(category.getDescription() != null ? category.getDescription().trim() : null);

        if (category.getStatus() == null || category.getStatus().isBlank()) {
            existing.setStatus("ACTIVE");
        } else {
            existing.setStatus(category.getStatus().trim().toUpperCase());
        }

        if (existing.getCreatedAt() == null) {
            existing.setCreatedAt(LocalDateTime.now());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(existing);
    }

    // ✅ REAL DELETE
    public void deleteCategory(Long id) {

        if (id == null) {
            throw new RuntimeException("Category id is required");
        }

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // category સાથે જોડાયેલા બધા products લાવો
        List<Product> products = productRepository.findByCategoryId(id);

        // પહેલાં products માંથી category remove કરો
        if (products != null && !products.isEmpty()) {
            for (Product p : products) {
                p.setCategory(null);
                p.setUpdatedAt(LocalDateTime.now());
            }
            productRepository.saveAll(products);
        }

        // પછી category actual delete કરો
        categoryRepository.delete(existing);
    }
}