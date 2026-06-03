package com.eventhub.eventservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ticket_types")
public class TicketType {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int totalQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketTypeStatus status;

    protected TicketType() {
    }

    public static TicketType create(
            UUID id,
            Event event,
            String name,
            BigDecimal price,
            int totalQuantity,
            TicketTypeStatus status
    ) {
        var ticketType = new TicketType();
        ticketType.id = id;
        ticketType.event = event;
        ticketType.name = name;
        ticketType.price = price;
        ticketType.totalQuantity = totalQuantity;
        ticketType.status = status;
        return ticketType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public TicketTypeStatus getStatus() {
        return status;
    }

    public void setStatus(TicketTypeStatus status) {
        this.status = status;
    }
}
