package com.eventhub.eventservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "competitions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Competition {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SportType sportType;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionStatus status;

    public static Competition create(UUID id, String name, SportType sportType, Instant startDate, Instant endDate, CompetitionStatus status) {
        var competition = new Competition();
        competition.id = id;
        competition.name = name;
        competition.sportType = sportType;
        competition.startDate = startDate;
        competition.endDate = endDate;
        competition.status = status;
        return competition;
    }

}
