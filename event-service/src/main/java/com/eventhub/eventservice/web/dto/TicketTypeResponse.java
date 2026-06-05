package com.eventhub.eventservice.web.dto;

import com.eventhub.eventservice.domain.TicketTypeStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record TicketTypeResponse(
        UUID id,
        UUID eventId,
        String name,
        String sectionName,
        BigDecimal price,
        String currency,
        int totalQuantity,
        int availableQuantity,
        TicketTypeStatus status
) {
}
