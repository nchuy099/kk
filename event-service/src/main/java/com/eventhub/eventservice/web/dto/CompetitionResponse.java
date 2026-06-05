package com.eventhub.eventservice.web.dto;

import com.eventhub.eventservice.domain.CompetitionStatus;
import com.eventhub.eventservice.domain.SportType;
import java.time.Instant;
import java.util.UUID;

public record CompetitionResponse(
        UUID id,
        String name,
        SportType sportType,
        Instant startDate,
        Instant endDate,
        CompetitionStatus status
) {
}
