package com.eventhub.eventservice.web.dto;

import com.eventhub.eventservice.domain.EventStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String name,
        String description,
        Instant startTime,
        Instant saleStartTime,
        Instant saleEndTime,
        EventStatus status,
        String venue,
        List<TicketTypeResponse> ticketTypes
) {
}

