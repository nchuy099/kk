package com.eventhub.ticketservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_issuances")
public class TicketIssuance {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false)
    private Instant createdAt;

    protected TicketIssuance() {
    }

    public static TicketIssuance create(UUID orderId) {
        var issuance = new TicketIssuance();
        issuance.id = UUID.randomUUID();
        issuance.orderId = orderId;
        issuance.createdAt = Instant.now();
        return issuance;
    }
}

