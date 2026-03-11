package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.dto.PaymentCreateRequest;
import com.milk_ecommerce_backend.dto.PaymentResponse;
import com.milk_ecommerce_backend.dto.PaymentVerifyRequest;
import com.milk_ecommerce_backend.dto.payment.PaymentInitiateRequest;
import com.milk_ecommerce_backend.dto.payment.PaymentInitiateResponse;
import com.milk_ecommerce_backend.dto.payment.PaymentSummaryResponse;
import com.milk_ecommerce_backend.dto.payment.RazorpayVerifyRequest;
import com.milk_ecommerce_backend.dto.payment.RazorpayVerifyResponse;
import com.milk_ecommerce_backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public PaymentInitiateResponse initiate(Authentication auth, @Valid @RequestBody PaymentInitiateRequest req) {
        return paymentService.initiateForOrder(auth.getName(), req);
    }

    @PostMapping("/razorpay/verify")
    public RazorpayVerifyResponse verifyRazorpay(Authentication auth, @RequestBody RazorpayVerifyRequest req) {
        return paymentService.verifyRazorpay(auth.getName(), req);
    }

    @GetMapping("/my")
    public List<PaymentSummaryResponse> my(Authentication auth) {
        return paymentService.listMyPayments(auth.getName());
    }

    @GetMapping("/my/order/{orderId}")
    public PaymentSummaryResponse myByOrder(Authentication auth, @PathVariable Long orderId) {
        return paymentService.getMyPaymentByOrderId(auth.getName(), orderId);
    }

    @PostMapping("/create")
    public PaymentResponse create(Authentication auth, @RequestBody(required = false) PaymentCreateRequest req) {
        return paymentService.create(auth.getName(), req);
    }

    @PostMapping("/verify")
    public PaymentResponse verify(Authentication auth, @RequestBody(required = false) PaymentVerifyRequest req) {
        return paymentService.verify(req);
    }
}