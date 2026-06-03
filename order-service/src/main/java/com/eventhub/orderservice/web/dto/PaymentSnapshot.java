package com.eventhub.orderservice.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSnapshot(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String status
) {
}

