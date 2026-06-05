package com.eventhub.eventservice.web.dto;

import com.eventhub.eventservice.domain.EventStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID competitionId,
        UUID stadiumId,
        String name,
        String description,
        String homeTeam,
        String awayTeam,
        Instant startTime,
        Instant saleStartTime,
        Instant saleEndTime,
        EventStatus status,
        List<TicketTypeResponse> ticketCategories
) {
}
