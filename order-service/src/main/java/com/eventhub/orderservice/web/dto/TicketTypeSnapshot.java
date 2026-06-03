package com.eventhub.orderservice.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketTypeSnapshot(
        UUID id,
        UUID eventId,
        String name,
        BigDecimal price,
        int totalQuantity,
        String status
) {
}

