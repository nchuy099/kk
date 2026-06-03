package com.eventhub.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        UUID reservationId,
        UUID paymentId,
        String userId,
        UUID ticketTypeId,
        int quantity,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}

