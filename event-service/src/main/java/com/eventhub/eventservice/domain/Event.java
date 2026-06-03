package com.eventhub.eventservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant saleStartTime;

    @Column(nullable = false)
    private Instant saleEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false)
    private String venue;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TicketType> ticketTypes = new ArrayList<>();

    protected Event() {
    }

    public static Event create(
            UUID id,
            String name,
            String description,
            Instant startTime,
            Instant saleStartTime,
            Instant saleEndTime,
            EventStatus status,
            String venue
    ) {
        var event = new Event();
        event.id = id;
        event.name = name;
        event.description = description;
        event.startTime = startTime;
        event.saleStartTime = saleStartTime;
        event.saleEndTime = saleEndTime;
        event.status = status;
        event.venue = venue;
        return event;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getSaleStartTime() {
        return saleStartTime;
    }

    public void setSaleStartTime(Instant saleStartTime) {
        this.saleStartTime = saleStartTime;
    }

    public Instant getSaleEndTime() {
        return saleEndTime;
    }

    public void setSaleEndTime(Instant saleEndTime) {
        this.saleEndTime = saleEndTime;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public List<TicketType> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }
}
