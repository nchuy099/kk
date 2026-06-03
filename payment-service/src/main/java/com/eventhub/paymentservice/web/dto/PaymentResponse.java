package com.eventhub.paymentservice.web.dto;

import com.eventhub.paymentservice.domain.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId
) {
}

