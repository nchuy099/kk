package com.eventhub.inventoryservice.web.dto;

import com.eventhub.inventoryservice.domain.ReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String userId,
        UUID ticketTypeId,
        int quantity,
        ReservationStatus status,
        Instant expiresAt,
        UUID orderId
) {
}

