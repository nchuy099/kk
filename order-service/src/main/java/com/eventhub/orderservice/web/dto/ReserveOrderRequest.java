package com.eventhub.orderservice.web.dto;

import java.util.UUID;

public record ReserveOrderRequest(
        String userId,
        UUID ticketTypeId,
        UUID orderId,
        int quantity
) {
}

