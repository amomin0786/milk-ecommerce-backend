package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.repository.UserRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminDeshbordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    @GetMapping("/dashboard")
    public Map<String,Object> dashboard(){

        Map<String,Object> data = new HashMap<>();

        data.put("users", userRepository.count());
        data.put("sellers", sellerRepository.count());
        data.put("products", productRepository.count());
        data.put("orders", orderRepository.count());

        return data;
    }

}