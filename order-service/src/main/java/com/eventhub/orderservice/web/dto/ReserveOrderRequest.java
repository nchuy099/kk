package com.eventhub.orderservice.web.dto;

import java.util.UUID;

public record ReserveOrderRequest(
        String userId,
        UUID ticketCategoryId,
        UUID orderId,
        int quantity
) {
}
