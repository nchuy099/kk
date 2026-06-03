package com.eventhub.eventservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateEventRequest(
        @NotBlank String name,
        String description,
        @NotNull Instant startTime,
        @NotNull Instant saleStartTime,
        @NotNull Instant saleEndTime,
        @NotBlank String venue
) {
}

