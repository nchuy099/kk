package com.eventhub.eventservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant saleStartTime;

    @Column(nullable = false)
    private Instant saleEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TicketType> ticketTypes = new ArrayList<>();

    public static Event create(
            UUID id,
            String name,
            String description,
            Competition competition,
            Stadium stadium,
            String homeTeam,
            String awayTeam,
            Instant startTime,
            Instant saleStartTime,
            Instant saleEndTime,
            EventStatus status
    ) {
        var event = new Event();
        event.id = id;
        event.name = name;
        event.description = description;
        event.competition = competition;
        event.stadium = stadium;
        event.homeTeam = homeTeam;
        event.awayTeam = awayTeam;
        event.startTime = startTime;
        event.saleStartTime = saleStartTime;
        event.saleEndTime = saleEndTime;
        event.status = status;
        return event;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public void setStadium(Stadium stadium) {
        this.stadium = stadium;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setSaleStartTime(Instant saleStartTime) {
        this.saleStartTime = saleStartTime;
    }

    public void setSaleEndTime(Instant saleEndTime) {
        this.saleEndTime = saleEndTime;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public void setTicketTypes(List<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }
}
