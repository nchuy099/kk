package com.eventhub.orderservice.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        UUID orderId,
        BigDecimal amount
) {
}

