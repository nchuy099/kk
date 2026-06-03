package com.eventhub.ticketservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private UUID ticketTypeId;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    @Column(nullable = false)
    private String qrCodeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private Instant issuedAt;

    protected Ticket() {
    }

    public static Ticket create(UUID orderId, String userId, UUID ticketTypeId, String ticketCode) {
        var ticket = new Ticket();
        ticket.id = UUID.randomUUID();
        ticket.orderId = orderId;
        ticket.userId = userId;
        ticket.ticketTypeId = ticketTypeId;
        ticket.ticketCode = ticketCode;
        ticket.qrCodeUrl = "qr://" + ticketCode;
        ticket.status = TicketStatus.ACTIVE;
        ticket.issuedAt = Instant.now();
        return ticket;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public UUID getTicketTypeId() {
        return ticketTypeId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void markUsed() {
        this.status = TicketStatus.USED;
    }
}

