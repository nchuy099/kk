package com.eventhub.orderservice.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationSnapshot(
        UUID id,
        String userId,
        UUID ticketTypeId,
        int quantity,
        String status,
        Instant expiresAt,
        UUID orderId
) {
}

