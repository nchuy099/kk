package com.eventhub.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID ticketCategoryId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    public static OrderItem create(UUID ticketCategoryId, int quantity, BigDecimal unitPrice) {
        var item = new OrderItem();
        item.id = UUID.randomUUID();
        item.ticketCategoryId = ticketCategoryId;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        return item;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
