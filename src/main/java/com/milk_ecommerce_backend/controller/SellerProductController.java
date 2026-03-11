package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.SellerProductRequest;
import com.milk_ecommerce_backend.dto.SellerProductResponse;
import com.milk_ecommerce_backend.dto.UpdateStockRequest;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.service.SellerProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
@CrossOrigin(origins = "http://localhost:4200")
public class SellerProductController {

    private final SellerProductService sellerProductService;

    public SellerProductController(SellerProductService sellerProductService) {
        this.sellerProductService = sellerProductService;
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public SellerProductResponse createProduct(
            Authentication auth,
            @Valid @RequestBody SellerProductRequest request
    ) {
        Product product = sellerProductService.createProduct(auth.getName(), request);
        return new SellerProductResponse(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping
    public List<SellerProductResponse> getMyProducts(Authentication auth) {
        return sellerProductService.getMyProducts(auth.getName())
                .stream()
                .map(SellerProductResponse::new)
                .toList();
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{productId}")
    public SellerProductResponse getMyProductById(Authentication auth, @PathVariable Long productId) {
        return new SellerProductResponse(
                sellerProductService.getMyProductById(auth.getName(), productId)
        );
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{productId}")
    public SellerProductResponse updateMyProduct(
            Authentication auth,
            @PathVariable Long productId,
            @Valid @RequestBody SellerProductRequest request
    ) {
        Product product = sellerProductService.updateMyProduct(auth.getName(), productId, request);
        return new SellerProductResponse(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PatchMapping("/{productId}/stock")
    public SellerProductResponse updateStock(
            Authentication auth,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        Product product = sellerProductService.updateStock(auth.getName(), productId, request);
        return new SellerProductResponse(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{productId}")
    public String deleteMyProduct(Authentication auth, @PathVariable Long productId) {
        sellerProductService.deleteMyProduct(auth.getName(), productId);
        return "Product deleted successfully";
    }
}