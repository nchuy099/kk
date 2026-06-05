package com.eventhub.inventoryservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private UUID ticketCategoryId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected Reservation() {
    }

    public static Reservation create(String userId, UUID ticketCategoryId, int quantity, UUID orderId, Instant expiresAt) {
        var reservation = new Reservation();
        reservation.id = UUID.randomUUID();
        reservation.userId = userId;
        reservation.ticketCategoryId = ticketCategoryId;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.RESERVED;
        reservation.orderId = orderId;
        reservation.expiresAt = expiresAt;
        reservation.createdAt = Instant.now();
        reservation.updatedAt = reservation.createdAt;
        return reservation;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public UUID getTicketCategoryId() {
        return ticketCategoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markConfirmed() {
        status = ReservationStatus.CONFIRMED;
        updatedAt = Instant.now();
    }

    public void markReleased() {
        status = ReservationStatus.RELEASED;
        updatedAt = Instant.now();
    }

    public void markExpired() {
        status = ReservationStatus.EXPIRED;
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == ReservationStatus.RESERVED;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
