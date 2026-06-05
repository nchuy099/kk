package com.eventhub.orderservice.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationSnapshot(
        UUID id,
        String userId,
        UUID ticketCategoryId,
        int quantity,
        String status,
        Instant expiresAt,
        UUID orderId
) {
}
