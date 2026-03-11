package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.PublicProductResponse;
import com.milk_ecommerce_backend.model.Category;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.repository.CategoryRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public Product addProduct(Long sellerId, Long categoryId, Product productRequest) {

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (!"APPROVED".equalsIgnoreCase(seller.getApprovalStatus())) {
            throw new RuntimeException("Seller not approved");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        productRequest.setSeller(seller);
        productRequest.setCategory(category);
        productRequest.setCreatedAt(LocalDateTime.now());
        productRequest.setUpdatedAt(LocalDateTime.now());

        if (productRequest.getStatus() == null || productRequest.getStatus().isBlank()) {
            productRequest.setStatus("ACTIVE");
        } else {
            productRequest.setStatus(productRequest.getStatus().trim().toUpperCase());
        }

        return productRepository.save(productRequest);
    }

    public List<PublicProductResponse> getAllActiveProducts() {
        return productRepository.findPublicProductsByStatus("ACTIVE");
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByStatus("ACTIVE", pageable);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySeller_Id(sellerId);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStatus("INACTIVE");
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    public Page<Product> searchProducts(String name, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable) {

        String status = "ACTIVE";

        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasCategory = categoryId != null;
        boolean hasPrice = minPrice != null && maxPrice != null;

        if (hasName) {
            return productRepository.findByStatusAndNameContainingIgnoreCase(status, name.trim(), pageable);
        }

        if (hasCategory && hasPrice) {
            return productRepository.findByStatusAndCategoryIdAndPriceBetween(status, categoryId, minPrice, maxPrice, pageable);
        }

        if (hasCategory) {
            return productRepository.findByStatusAndCategoryId(status, categoryId, pageable);
        }

        if (hasPrice) {
            return productRepository.findByStatusAndPriceBetween(status, minPrice, maxPrice, pageable);
        }

        return productRepository.findByStatus(status, pageable);
    }
}