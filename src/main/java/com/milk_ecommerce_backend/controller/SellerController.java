package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.SellerApplyRequest;
import com.milk_ecommerce_backend.dto.SellerResponse;
import com.milk_ecommerce_backend.dto.seller.SellerDashboardResponse;
import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.service.SellerDashboardService;
import com.milk_ecommerce_backend.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sellers")
@CrossOrigin(origins = "http://localhost:4200")
public class SellerController {

    private final SellerService sellerService;
    private final SellerDashboardService sellerDashboardService;

    public SellerController(SellerService sellerService, SellerDashboardService sellerDashboardService) {
        this.sellerService = sellerService;
        this.sellerDashboardService = sellerDashboardService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/apply")
    public SellerResponse apply(Authentication auth, @Valid @RequestBody SellerApplyRequest req) {
        Seller s = sellerService.apply(auth.getName(), req);
        return new SellerResponse(s);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public List<SellerResponse> pending() {
        return sellerService.pending().stream().map(SellerResponse::new).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<SellerResponse> all() {
        return sellerService.all().stream().map(SellerResponse::new).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{sellerId}/approve")
    public SellerResponse approve(@PathVariable Long sellerId) {
        return new SellerResponse(sellerService.approve(sellerId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{sellerId}/reject")
    public SellerResponse reject(@PathVariable Long sellerId) {
        return new SellerResponse(sellerService.reject(sellerId));
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/dashboard")
    public SellerDashboardResponse dashboard(Authentication auth) {
        return sellerService.getDashboard(auth.getName());
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/reports")
    public Map<String, Object> reports(
            Authentication auth,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return sellerDashboardService.getReports(auth.getName(), from, to);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportReports(
            Authentication auth,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return sellerDashboardService.exportReportsCsv(auth.getName(), from, to);
    }
}