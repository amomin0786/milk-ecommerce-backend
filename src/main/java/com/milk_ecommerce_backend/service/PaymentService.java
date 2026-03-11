package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.PaymentCreateRequest;
import com.milk_ecommerce_backend.dto.PaymentResponse;
import com.milk_ecommerce_backend.dto.PaymentVerifyRequest;
import com.milk_ecommerce_backend.dto.payment.PaymentInitiateRequest;
import com.milk_ecommerce_backend.dto.payment.PaymentInitiateResponse;
import com.milk_ecommerce_backend.dto.payment.PaymentSummaryResponse;
import com.milk_ecommerce_backend.dto.payment.RazorpayVerifyRequest;
import com.milk_ecommerce_backend.dto.payment.RazorpayVerifyResponse;
import com.milk_ecommerce_backend.exception.NotFoundException;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.Payment;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.OrderRepository;
import com.milk_ecommerce_backend.repository.PaymentRepository;
import com.milk_ecommerce_backend.repository.UserRepository;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final String DEFAULT_CURRENCY = "INR";

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.enabled:false}")
    private boolean razorpayEnabled;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentInitiateResponse initiateForOrder(String userEmail, PaymentInitiateRequest req) {

        if (req == null || req.getOrderId() == null) {
            throw new RuntimeException("orderId required");
        }

        String method = normalizeMethod(req.getMethod());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findByIdAndUserId(req.getOrderId(), user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        BigDecimal amount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order amount invalid");
        }

        Payment existing = paymentRepository.findByOrder_Id(order.getId()).orElse(null);
        if (existing != null) {
            if ("RAZORPAY".equals(method) && isBlank(existing.getProviderOrderId())) {
                createRazorpayOrder(existing);
                existing = paymentRepository.save(existing);
            }
            return toInitiateResponse(existing);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(amount);
        payment.setCurrency(DEFAULT_CURRENCY);
        payment.setTransactionId(generateTxnId());

        if ("COD".equals(method)) {
            payment.setProvider("COD");
            payment.setPaymentStatus("PENDING");
        } else {
            payment.setProvider("RAZORPAY");
            payment.setPaymentStatus("INITIATED");
            createRazorpayOrder(payment);
        }

        Payment saved = paymentRepository.save(payment);
        return toInitiateResponse(saved);
    }

    @Transactional
    public RazorpayVerifyResponse verifyRazorpay(String userEmail, RazorpayVerifyRequest req) {

        if (req == null || req.getPaymentId() == null) {
            throw new RuntimeException("paymentId required");
        }

        if (isBlank(req.getRazorpayOrderId())
                || isBlank(req.getRazorpayPaymentId())
                || isBlank(req.getRazorpaySignature())) {
            throw new RuntimeException("Razorpay verification details are required");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        Order order = payment.getOrder();
        if (order == null || order.getUser() == null || order.getUser().getId() == null
                || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not allowed");
        }

        if (!"RAZORPAY".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new RuntimeException("This payment is not a Razorpay payment");
        }

        String serverOrderId = payment.getProviderOrderId();
        if (isBlank(serverOrderId)) {
            throw new RuntimeException("Provider order id missing");
        }

        if (!serverOrderId.equals(req.getRazorpayOrderId())) {
            throw new RuntimeException("Razorpay order mismatch");
        }

        String generatedSignature = hmacSha256(
                serverOrderId + "|" + req.getRazorpayPaymentId(),
                razorpayKeySecret
        );

        boolean verified = generatedSignature.equals(req.getRazorpaySignature());

        if (!verified) {
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);

            return new RazorpayVerifyResponse(
                    false,
                    payment.getId(),
                    order.getId(),
                    payment.getPaymentStatus(),
                    "Signature verification failed"
            );
        }

        payment.setProvider("RAZORPAY");
        payment.setProviderOrderId(req.getRazorpayOrderId());
        payment.setProviderPaymentId(req.getRazorpayPaymentId());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setPaidDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        if ("PENDING".equalsIgnoreCase(order.getOrderStatus())) {
            order.setOrderStatus("PAID");
        }

        orderRepository.save(order);

        return new RazorpayVerifyResponse(
                true,
                payment.getId(),
                order.getId(),
                payment.getPaymentStatus(),
                "Payment verified successfully"
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentSummaryResponse> listMyPayments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return paymentRepository.findByOrder_User_Id(user.getId())
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getMyPaymentByOrderId(String userEmail, Long orderId) {
        if (orderId == null) {
            throw new RuntimeException("orderId required");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for this order"));

        return toSummaryResponse(payment);
    }

    @Transactional
    public PaymentResponse create(String userEmail, PaymentCreateRequest req) {

        Long orderId = (req == null) ? null : req.getOrderId();
        if (orderId != null) {
            PaymentInitiateRequest request = new PaymentInitiateRequest();
            request.setOrderId(orderId);
            request.setMethod((req.getMethod() == null) ? "RAZORPAY" : req.getMethod());

            PaymentInitiateResponse initiated = initiateForOrder(userEmail, request);

            return new PaymentResponse(
                    String.valueOf(initiated.getPaymentId()),
                    initiated.getAmount(),
                    initiated.getPaymentMethod(),
                    initiated.getPaymentStatus(),
                    false
            );
        }

        BigDecimal amount = (req == null || req.getAmount() == null)
                ? BigDecimal.ZERO
                : req.getAmount();

        String method = (req == null || req.getMethod() == null)
                ? "RAZORPAY"
                : req.getMethod().trim().toUpperCase();

        if (!"RAZORPAY".equals(method)) {
            throw new RuntimeException("Invalid payment method");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        String paymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();

        return new PaymentResponse(paymentId, amount, method, "CREATED", false);
    }

    public PaymentResponse verify(PaymentVerifyRequest req) {
        if (req == null || req.getPaymentId() == null || req.getPaymentId().isBlank()) {
            throw new RuntimeException("paymentId required");
        }

        String status = (req.getStatus() == null)
                ? "FAILED"
                : req.getStatus().trim().toUpperCase();

        boolean ok = "SUCCESS".equals(status);

        return new PaymentResponse(req.getPaymentId().trim(), null, null, status, ok);
    }

    private void createRazorpayOrder(Payment payment) {
        if (!razorpayEnabled) {
            throw new RuntimeException("Razorpay is disabled. Set razorpay.enabled=true and configure keys.");
        }

        if (isBlank(razorpayKeyId) || isBlank(razorpayKeySecret)) {
            throw new RuntimeException("Razorpay keys are missing");
        }

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
            options.put("currency", DEFAULT_CURRENCY);
            options.put("receipt", payment.getTransactionId());

            com.razorpay.Order razorpayOrder = client.orders.create(options);

            payment.setProvider("RAZORPAY");
            payment.setProviderOrderId(razorpayOrder.get("id"));

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Razorpay order: " + ex.getMessage());
        }
    }

    private PaymentInitiateResponse toInitiateResponse(Payment payment) {
        PaymentInitiateResponse response = new PaymentInitiateResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getCreatedAt()
        );

        response.setKeyId(razorpayKeyId);
        response.setProviderOrderId(payment.getProviderOrderId());

        return response;
    }

    private PaymentSummaryResponse toSummaryResponse(Payment payment) {
        return new PaymentSummaryResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentDate()
        );
    }

    private String generateTxnId() {
        String s = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "TXN_" + s.substring(0, 16);
    }

    private String normalizeMethod(String method) {
        String m = method == null ? "" : method.trim().toUpperCase();

        if (m.isBlank()) {
            return "COD";
        }

        if (!m.equals("COD") && !m.equals("RAZORPAY")) {
            throw new RuntimeException("Invalid payment method. Allowed: COD, RAZORPAY");
        }

        return m;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception ex) {
            throw new RuntimeException("Signature generation failed");
        }
    }

    private String toHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String out = formatter.toString();
        formatter.close();
        return out;
    }
}