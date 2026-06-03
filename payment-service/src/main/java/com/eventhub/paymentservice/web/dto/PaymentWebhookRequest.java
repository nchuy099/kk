package com.eventhub.paymentservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentWebhookRequest(
        @NotBlank String providerEventId,
        @NotBlank String transactionId,
        @NotNull UUID orderId,
        @NotBlank String status,
        @NotNull BigDecimal amount
) {
}

