package com.eventhub.paymentservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private Instant createdAt;

    protected Payment() {
    }

    public static Payment create(UUID orderId, BigDecimal amount, String currency) {
        var payment = new Payment();
        payment.id = UUID.randomUUID();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.currency = currency;
        payment.provider = "MOCK_PROVIDER";
        payment.status = PaymentStatus.PENDING;
        payment.transactionId = "txn-" + UUID.randomUUID();
        payment.createdAt = Instant.now();
        return payment;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getProvider() {
        return provider;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markSuccess(String transactionId) {
        this.status = PaymentStatus.SUCCEEDED;
        this.transactionId = transactionId;
    }

    public void markFailed(String transactionId) {
        this.status = PaymentStatus.FAILED;
        this.transactionId = transactionId;
    }
}
