package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.service.SellerDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seller/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class SellerDashboardController {

    @Autowired
    private SellerDashboardService dashboardService;

    @GetMapping
    public Map<String,Object> dashboard(Authentication auth){

        String email = auth.getName();

        return dashboardService.getDashboard(email);
    }
}