package com.eventhub.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(unique = true)
    private UUID reservationId;

    @Column
    private UUID paymentId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private String failureReason;

    @OneToMany(mappedBy = "order", orphanRemoval = true, cascade = jakarta.persistence.CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(UUID id, String userId, UUID eventId, UUID reservationId, BigDecimal totalAmount, String currency, Instant expiresAt) {
        var order = new Order();
        order.id = id;
        order.userId = userId;
        order.eventId = eventId;
        order.reservationId = reservationId;
        order.totalAmount = totalAmount;
        order.currency = currency;
        order.status = OrderStatus.PENDING;
        order.expiresAt = expiresAt;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        return order;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
        this.updatedAt = Instant.now();
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
        this.updatedAt = Instant.now();
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }
}
