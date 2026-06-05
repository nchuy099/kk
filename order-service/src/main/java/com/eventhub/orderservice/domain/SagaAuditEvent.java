package com.eventhub.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "saga_audit_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaAuditEvent {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sagaId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String status;

    @Column
    private String reason;

    @Column(nullable = false)
    private Instant createdAt;

    public static SagaAuditEvent create(UUID sagaId, UUID orderId, String eventType, String serviceName, String status, String reason) {
        var event = new SagaAuditEvent();
        event.id = UUID.randomUUID();
        event.sagaId = sagaId;
        event.orderId = orderId;
        event.eventType = eventType;
        event.serviceName = serviceName;
        event.status = status;
        event.reason = reason;
        event.createdAt = Instant.now();
        return event;
    }
}
