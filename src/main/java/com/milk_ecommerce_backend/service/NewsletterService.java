package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.model.Subscriber;
import com.milk_ecommerce_backend.repository.SubscriberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewsletterService {

    @Autowired
    private SubscriberRepository repository;

    public String subscribe(String email) {

        if (email == null || !email.contains("@")) {
            throw new RuntimeException("Invalid email");
        }

        if (repository.existsByEmail(email)) {
            return "Already subscribed";
        }

        Subscriber s = new Subscriber();
        s.setEmail(email);

        repository.save(s);

        return "Subscribed successfully";
    }
}