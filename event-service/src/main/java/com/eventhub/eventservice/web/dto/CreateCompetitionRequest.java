package com.eventhub.eventservice.web.dto;

import com.eventhub.eventservice.domain.SportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateCompetitionRequest(
        @NotBlank String name,
        @NotNull SportType sportType,
        @NotNull Instant startDate,
        @NotNull Instant endDate
) {
}
