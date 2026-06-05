package com.eventhub.orderservice.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketTypeSnapshot(
        UUID id,
        UUID eventId,
        String name,
        String sectionName,
        BigDecimal price,
        String currency,
        int totalQuantity,
        String status
) {
}
