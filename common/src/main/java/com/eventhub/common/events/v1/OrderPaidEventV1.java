package com.eventhub.common.events.v1;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPaidEventV1(
        String eventId,
        String eventType,
        String eventVersion,
        String correlationId,
        Instant occurredAt,
        UUID orderId,
        UUID reservationId,
        UUID paymentId,
        String userId,
        UUID ticketTypeId,
        int quantity,
        BigDecimal totalAmount
) {
}
