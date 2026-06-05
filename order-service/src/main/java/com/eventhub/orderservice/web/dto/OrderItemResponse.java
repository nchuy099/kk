package com.eventhub.orderservice.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID ticketCategoryId,
        int quantity,
        BigDecimal unitPrice
) {
}
