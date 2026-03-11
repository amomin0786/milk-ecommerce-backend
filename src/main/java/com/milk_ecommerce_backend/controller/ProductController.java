package com.milk_ecommerce_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milk_ecommerce_backend.dto.PublicProductResponse;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.service.FileUploadService;
import com.milk_ecommerce_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/all")
    public List<PublicProductResponse> getAllActiveProducts() {
        return productService.getAllActiveProducts();
    }

    @GetMapping
    public Page<Product> getAllProducts(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping(
            value = "/add/{sellerId}/{categoryId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Product> addProductJson(
            @PathVariable Long sellerId,
            @PathVariable Long categoryId,
            @RequestBody Product product
    ) {
        Product savedProduct = productService.addProduct(sellerId, categoryId, product);
        return ResponseEntity.ok(savedProduct);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping(
            value = "/add/{sellerId}/{categoryId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Product> addProductMultipart(
            @PathVariable Long sellerId,
            @PathVariable Long categoryId,
            @RequestParam("product") String productJson,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Product product = mapper.readValue(productJson, Product.class);

            String fileName = fileUploadService.uploadFile(image);
            product.setImageUrl("http://localhost:8080/uploads/" + fileName);

            Product savedProduct = productService.addProduct(sellerId, categoryId, product);
            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            throw new RuntimeException("Product add failed", e);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/{sellerId}")
    public List<Product> getSellerProducts(@PathVariable Long sellerId) {
        return productService.getProductsBySeller(sellerId);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/search")
    public Page<Product> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable
    ) {
        return productService.searchProducts(name, categoryId, minPrice, maxPrice, pageable);
    }
}