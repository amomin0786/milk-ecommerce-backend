package com.milk_ecommerce_backend.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSummaryResponse {

    private Long paymentId;
    private Long orderId;

    private String paymentStatus;
    private String paymentMethod;

    private String transactionId;

    private BigDecimal amount;
    private String currency;

    private LocalDateTime paymentDate;

    public PaymentSummaryResponse() {}

    public PaymentSummaryResponse(Long paymentId, Long orderId, String paymentStatus, String paymentMethod,
                                  String transactionId, BigDecimal amount, String currency, LocalDateTime paymentDate) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.paymentDate = paymentDate;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}