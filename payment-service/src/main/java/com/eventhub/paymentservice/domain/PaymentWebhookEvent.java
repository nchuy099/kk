package com.eventhub.paymentservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_webhook_events")
public class PaymentWebhookEvent {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String providerEventId;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "text", nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant processedAt;

    protected PaymentWebhookEvent() {
    }

    public static PaymentWebhookEvent create(String providerEventId, String transactionId, String status, String payload) {
        var event = new PaymentWebhookEvent();
        event.id = UUID.randomUUID();
        event.providerEventId = providerEventId;
        event.transactionId = transactionId;
        event.status = status;
        event.payload = payload;
        event.processedAt = Instant.now();
        return event;
    }
}

