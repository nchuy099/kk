package com.eventhub.orderservice.web.dto;

import com.eventhub.orderservice.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String userId,
        UUID eventId,
        UUID reservationId,
        UUID paymentId,
        BigDecimal totalAmount,
        String currency,
        OrderStatus status,
        Instant expiresAt,
        List<OrderItemResponse> items
) {
}
