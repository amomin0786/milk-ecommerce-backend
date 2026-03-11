package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.SellerProductRequest;
import com.milk_ecommerce_backend.dto.UpdateStockRequest;
import com.milk_ecommerce_backend.exception.NotFoundException;
import com.milk_ecommerce_backend.model.Category;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.CategoryRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import com.milk_ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SellerProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public SellerProductService(
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Product createProduct(String email, SellerProductRequest request) {

        Seller seller = getApprovedSellerByEmail(email);

        if (request.getCategoryId() == null) {
            throw new RuntimeException("Category is required");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);

        product.setStatus(
                request.getStatus() != null && !request.getStatus().isBlank()
                        ? request.getStatus().trim().toUpperCase()
                        : "ACTIVE"
        );

        product.setSeller(seller);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);

        return productRepository.findByIdAndSellerIdWithRelations(saved.getId(), seller.getId())
                .orElseThrow(() -> new NotFoundException("Product not found after save"));
    }
    @Transactional(readOnly = true)
    public List<Product> getMyProducts(String email) {
        Seller seller = getApprovedSellerByEmail(email);
        return productRepository.findBySellerIdWithRelations(seller.getId());
    }

    @Transactional(readOnly = true)
    public Product getMyProductById(String email, Long productId) {
        Seller seller = getApprovedSellerByEmail(email);
        return productRepository.findByIdAndSellerIdWithRelations(productId, seller.getId())
                .orElseThrow(() -> new RuntimeException("You can access only your own product"));
    }

    @Transactional
    public Product updateMyProduct(String email, Long productId, SellerProductRequest request) {

        Seller seller = getApprovedSellerByEmail(email);

        Product product = productRepository.findByIdAndSeller_Id(productId, seller.getId())
                .orElseThrow(() -> new RuntimeException("You can update only your own product"));

        if (request.getCategoryId() == null) {
            throw new RuntimeException("Category is required");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);

        product.setStatus(
                request.getStatus() != null && !request.getStatus().isBlank()
                        ? request.getStatus().trim().toUpperCase()
                        : product.getStatus()
        );

        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);

        return productRepository.findByIdAndSellerIdWithRelations(saved.getId(), seller.getId())
                .orElseThrow(() -> new NotFoundException("Product not found after update"));
    }

    @Transactional
    public Product updateStock(String email, Long productId, UpdateStockRequest request) {
        Seller seller = getApprovedSellerByEmail(email);

        Product product = productRepository.findByIdAndSeller_Id(productId, seller.getId())
                .orElseThrow(() -> new RuntimeException("You can update only your own product stock"));

        product.setStock(request.getStock());
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);

        return productRepository.findByIdAndSellerIdWithRelations(saved.getId(), seller.getId())
                .orElseThrow(() -> new NotFoundException("Product not found after stock update"));
    }

    @Transactional
    public void deleteMyProduct(String email, Long productId) {
        Seller seller = getApprovedSellerByEmail(email);

        Product product = productRepository.findByIdAndSeller_Id(productId, seller.getId())
                .orElseThrow(() -> new RuntimeException("You can delete only your own product"));

        productRepository.delete(product);
    }

    private Seller getApprovedSellerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Seller seller = sellerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        if (!"APPROVED".equalsIgnoreCase(seller.getApprovalStatus())) {
            throw new RuntimeException("Seller account is not approved");
        }

        return seller;
    }
}