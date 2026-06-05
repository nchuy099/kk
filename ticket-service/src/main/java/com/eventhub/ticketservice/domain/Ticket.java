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
    private UUID ticketCategoryId;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    @Column(nullable = false)
    private String qrCodePayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private Instant issuedAt;

    protected Ticket() {
    }

    public static Ticket create(UUID orderId, String userId, UUID ticketCategoryId, String ticketCode) {
        var ticket = new Ticket();
        ticket.id = UUID.randomUUID();
        ticket.orderId = orderId;
        ticket.userId = userId;
        ticket.ticketCategoryId = ticketCategoryId;
        ticket.ticketCode = ticketCode;
        ticket.qrCodePayload = "qr://" + ticketCode;
        ticket.status = TicketStatus.ISSUED;
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

    public UUID getTicketCategoryId() {
        return ticketCategoryId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getQrCodePayload() {
        return qrCodePayload;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void markUsed() {
        this.status = TicketStatus.CHECKED_IN;
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }
}
