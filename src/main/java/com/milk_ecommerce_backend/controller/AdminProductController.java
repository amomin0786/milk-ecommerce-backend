package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.AdminProductResponse;
import com.milk_ecommerce_backend.service.AdminProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AdminProductResponse> getAllProducts() {
        return adminProductService.getAllProducts();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/status")
    public String updateProductStatus(
            @PathVariable Long productId,
            @RequestParam String status
    ) {
        adminProductService.updateProductStatus(productId, status);
        return "Product status updated successfully";
    }
}