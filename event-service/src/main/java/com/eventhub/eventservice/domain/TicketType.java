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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sectionName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private int totalQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketTypeStatus status;

    public static TicketType create(
            UUID id,
            Event event,
            String name,
            String sectionName,
            BigDecimal price,
            String currency,
            int totalQuantity,
            TicketTypeStatus status
    ) {
        var ticketType = new TicketType();
        ticketType.id = id;
        ticketType.event = event;
        ticketType.name = name;
        ticketType.sectionName = sectionName;
        ticketType.price = price;
        ticketType.currency = currency;
        ticketType.totalQuantity = totalQuantity;
        ticketType.status = status;
        return ticketType;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setStatus(TicketTypeStatus status) {
        this.status = status;
    }
}
