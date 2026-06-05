package com.eventhub.eventservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID competitionId,
        @NotNull UUID stadiumId,
        @NotBlank String homeTeam,
        @NotBlank String awayTeam,
        @NotNull Instant startTime,
        @NotNull Instant saleStartTime,
        @NotNull Instant saleEndTime
) {
}
