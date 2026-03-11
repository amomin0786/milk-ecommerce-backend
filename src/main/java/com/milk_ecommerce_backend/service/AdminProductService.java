package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.AdminProductResponse;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;

    public AdminProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // =========================
    // GET ALL PRODUCTS
    // =========================

    public List<AdminProductResponse> getAllProducts() {

        List<AdminProductResponse> list = productRepository.findAdminProducts();

        if (list == null) {
            throw new RuntimeException("Products not found");
        }

        return list;

    }

    // =========================
    // UPDATE PRODUCT STATUS
    // =========================

    public Product updateProductStatus(Long productId, String status) {

        if (productId == null) {
            throw new RuntimeException("Product id required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status required");
        }

        String s = status.trim().toUpperCase();

        if (!s.equals("ACTIVE") && !s.equals("INACTIVE")) {
            throw new RuntimeException("Invalid status");
        }

        product.setStatus(s);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);

    }

}