package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.service.NewsletterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "http://localhost:4200")
public class NewsletterController {

    @Autowired
    private NewsletterService service;

    @PostMapping("/subscribe")
    public Map<String, String> subscribe(@RequestBody Map<String,String> body) {

        String email = body.get("email");

        String result = service.subscribe(email);

        return Map.of("message", result);
    }
}