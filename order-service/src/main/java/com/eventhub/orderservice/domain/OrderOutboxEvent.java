package com.eventhub.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_outbox_events")
public class OrderOutboxEvent {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String eventVersion;

    @Column(columnDefinition = "text", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderOutboxEventStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant sentAt;

    protected OrderOutboxEvent() {
    }

    public static OrderOutboxEvent create(String aggregateType, String aggregateId, String eventType, String eventVersion, String payload) {
        var event = new OrderOutboxEvent();
        event.id = UUID.randomUUID();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.eventVersion = eventVersion;
        event.payload = payload;
        event.status = OrderOutboxEventStatus.PENDING;
        event.attempts = 0;
        event.createdAt = Instant.now();
        return event;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public String getPayload() {
        return payload;
    }

    public OrderOutboxEventStatus getStatus() {
        return status;
    }

    public void markSent() {
        this.status = OrderOutboxEventStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.attempts++;
        if (this.attempts >= 5) {
            this.status = OrderOutboxEventStatus.FAILED;
        }
    }
}
