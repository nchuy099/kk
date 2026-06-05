package com.eventhub.paymentservice.service;

import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.paymentservice.domain.Payment;
import com.eventhub.paymentservice.domain.PaymentOutboxEvent;
import com.eventhub.paymentservice.domain.PaymentStatus;
import com.eventhub.paymentservice.domain.PaymentWebhookEvent;
import com.eventhub.paymentservice.repository.PaymentRepository;
import com.eventhub.paymentservice.repository.PaymentOutboxEventRepository;
import com.eventhub.paymentservice.repository.PaymentWebhookEventRepository;
import com.eventhub.paymentservice.service.exception.NotFoundException;
import com.eventhub.paymentservice.service.exception.PaymentConflictException;
import com.eventhub.paymentservice.web.dto.CreatePaymentRequest;
import com.eventhub.paymentservice.web.dto.PaymentResponse;
import com.eventhub.paymentservice.web.dto.PaymentWebhookRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PaymentOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentWebhookEventRepository webhookEventRepository,
            PaymentOutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            Tracer tracer
    ) {
        this.paymentRepository = paymentRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        var existing = paymentRepository.findByOrderId(request.orderId());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        var payment = Payment.create(request.orderId(), request.amount(), request.currency());
        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse get(UUID paymentId) {
        return toResponse(findPayment(paymentId));
    }

    @Transactional
    public PaymentResponse mockSuccess(UUID paymentId) {
        var payment = findPayment(paymentId);
        var request = new PaymentWebhookRequest(
                "mock-" + paymentId,
                "txn-" + paymentId,
                payment.getOrderId(),
                "SUCCEEDED",
                payment.getAmount()
        );
        return handleWebhook(request);
    }

    @Transactional
    public PaymentResponse handleWebhook(PaymentWebhookRequest request) {
        var existingWebhook = webhookEventRepository.findByProviderEventId(request.providerEventId());
        if (existingWebhook.isPresent()) {
            return toResponse(findByOrderId(request.orderId()));
        }

        var payment = findByOrderId(request.orderId());
        if (webhookEventRepository.existsByTransactionId(request.transactionId())) {
            return toResponse(payment);
        }

        try {
            webhookEventRepository.save(PaymentWebhookEvent.create(
                    request.providerEventId(),
                    request.transactionId(),
                    request.status(),
                    requestToJson(request)
            ));
        } catch (DataIntegrityViolationException exception) {
            return toResponse(payment);
        }

        if ("SUCCEEDED".equalsIgnoreCase(request.status()) || "SUCCESS".equalsIgnoreCase(request.status())) {
            if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                payment.markSuccess(request.transactionId());
                payment = paymentRepository.save(payment);
                createPaymentSucceededOutboxEvent(payment);
            }
            return toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            payment.markFailed(request.transactionId());
            payment = paymentRepository.save(payment);
        }
        return toResponse(payment);
    }

    private void createPaymentSucceededOutboxEvent(Payment payment) {
        var event = new PaymentSucceededEventV1(
                UUID.randomUUID().toString(),
                "PaymentSucceededEvent",
                RabbitTopics.EVENT_VERSION_V1,
                currentCorrelationId(),
                Instant.now(),
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionId(),
                payment.getAmount()
        );
        try {
            outboxEventRepository.save(PaymentOutboxEvent.create(
                    "Payment",
                    payment.getId().toString(),
                    event.eventType(),
                    event.eventVersion(),
                    objectMapper.writeValueAsString(event)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize payment outbox event", exception);
        }
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
    }

    private Payment findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getTransactionId()
        );
    }

    private static String requestToJson(PaymentWebhookRequest request) {
        return "{\"providerEventId\":\"" + request.providerEventId() + "\",\"transactionId\":\"" + request.transactionId() + "\",\"orderId\":\"" + request.orderId() + "\",\"status\":\"" + request.status() + "\",\"amount\":\"" + request.amount() + "\"}";
    }

    private String currentCorrelationId() {
        var span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return span.context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
