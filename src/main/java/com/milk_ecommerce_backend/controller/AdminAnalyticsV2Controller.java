package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.api.ApiResponse;
import com.milk_ecommerce_backend.dto.admin.AdminAnalyticsResponse;
import com.milk_ecommerce_backend.service.AdminAnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsV2Controller {

    private final AdminAnalyticsService service;

    public AdminAnalyticsV2Controller(AdminAnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminAnalyticsResponse> dashboard() {
        return ApiResponse.ok("Analytics fetched", service.dashboard());
    }
}