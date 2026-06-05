package com.eventhub.paymentservice.service;

import com.eventhub.common.events.v1.OrderExpiredEventV1;
import com.eventhub.common.events.v1.PaymentCreatedEventV1;
import com.eventhub.common.events.v1.PaymentFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundFailedEventV1;
import com.eventhub.common.events.v1.PaymentRefundRequestedEventV1;
import com.eventhub.common.events.v1.PaymentRefundedEventV1;
import com.eventhub.common.events.v1.PaymentRequestedEventV1;
import com.eventhub.common.events.v1.PaymentSucceededEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.paymentservice.domain.Payment;
import com.eventhub.paymentservice.domain.PaymentOutboxEvent;
import com.eventhub.paymentservice.domain.PaymentStatus;
import com.eventhub.paymentservice.domain.PaymentWebhookEvent;
import com.eventhub.paymentservice.domain.ProcessedEvent;
import com.eventhub.paymentservice.repository.PaymentOutboxEventRepository;
import com.eventhub.paymentservice.repository.PaymentRepository;
import com.eventhub.paymentservice.repository.PaymentWebhookEventRepository;
import com.eventhub.paymentservice.repository.ProcessedEventRepository;
import com.eventhub.paymentservice.service.exception.NotFoundException;
import com.eventhub.paymentservice.service.exception.PaymentConflictException;
import com.eventhub.paymentservice.web.dto.CreatePaymentRequest;
import com.eventhub.paymentservice.web.dto.PaymentResponse;
import com.eventhub.paymentservice.web.dto.PaymentWebhookRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PaymentOutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentWebhookEventRepository webhookEventRepository,
            PaymentOutboxEventRepository outboxEventRepository,
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper,
            Tracer tracer
    ) {
        this.paymentRepository = paymentRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
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
        payment = paymentRepository.save(payment);
        createPaymentCreatedOutboxEvent(payment, request.orderId().toString(), null);
        return toResponse(payment);
    }

    public PaymentResponse get(UUID paymentId) {
        return toResponse(findPayment(paymentId));
    }

    public PaymentResponse getByOrderId(UUID orderId) {
        return toResponse(findByOrderId(orderId));
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
                createPaymentSucceededOutboxEvent(payment, request.providerEventId());
            }
            return toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.SUCCEEDED && payment.getStatus() != PaymentStatus.REFUNDED) {
            payment.markFailed(request.transactionId());
            payment = paymentRepository.save(payment);
            createPaymentFailedOutboxEvent(payment, request.providerEventId(), "Payment provider reported failure");
        }
        return toResponse(payment);
    }

    @Transactional
    public void handlePaymentRequested(PaymentRequestedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "paymentRequested")) {
            return;
        }
        var payment = paymentRepository.findByOrderId(event.orderId())
                .orElseGet(() -> paymentRepository.save(Payment.create(event.orderId(), event.amount(), event.currency())));
        createPaymentCreatedOutboxEvent(payment, event.correlationId(), event.eventId());
        markProcessed(event.eventId(), "paymentRequested");
    }

    @Transactional
    public void handleRefundRequested(PaymentRefundRequestedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "refundRequested")) {
            return;
        }
        var payment = findPayment(event.paymentId());
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            markProcessed(event.eventId(), "refundRequested");
            return;
        }
        payment.markRefundPending();
        paymentRepository.save(payment);
        if (event.reason() != null && event.reason().toLowerCase().contains("refund-fail")) {
            payment.markRefundFailed("refund-failed-" + event.paymentId());
            paymentRepository.save(payment);
            createPaymentRefundFailedOutboxEvent(payment, event.correlationId(), event.eventId(), "Refund failed in mock provider");
        } else {
            payment.markRefunded("refund-" + event.paymentId());
            paymentRepository.save(payment);
            createPaymentRefundedOutboxEvent(payment, event.correlationId(), event.eventId(), event.reason());
        }
        markProcessed(event.eventId(), "refundRequested");
    }

    @Transactional
    public void handleOrderExpired(OrderExpiredEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderExpired")) {
            return;
        }
        var payment = paymentRepository.findByOrderId(event.orderId()).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            payment.markCancelled();
            paymentRepository.save(payment);
        }
        markProcessed(event.eventId(), "orderExpired");
    }

    private void createPaymentCreatedOutboxEvent(Payment payment, String correlationId, String causationId) {
        var event = new PaymentCreatedEventV1(
                UUID.randomUUID().toString(),
                "PaymentCreatedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                payment.getOrderId(),
                payment.getOrderId(),
                correlationId != null ? correlationId : currentCorrelationId(),
                causationId,
                Instant.now(),
                null,
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider()
        );
        createOutboxEvent(payment.getId(), event.eventType(), event);
    }

    private void createPaymentSucceededOutboxEvent(Payment payment, String providerEventId) {
        var event = new PaymentSucceededEventV1(
                UUID.randomUUID().toString(),
                "PaymentSucceededEvent",
                RabbitTopics.EVENT_VERSION_V1,
                payment.getOrderId(),
                payment.getOrderId(),
                currentCorrelationId(),
                providerEventId,
                Instant.now(),
                null,
                payment.getId(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getCurrency()
        );
        createOutboxEvent(payment.getId(), event.eventType(), event);
    }

    private void createPaymentFailedOutboxEvent(Payment payment, String causationId, String reason) {
        var event = new PaymentFailedEventV1(
                UUID.randomUUID().toString(),
                "PaymentFailedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                payment.getOrderId(),
                payment.getOrderId(),
                currentCorrelationId(),
                causationId,
                Instant.now(),
                reason,
                payment.getId(),
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getCurrency()
        );
        createOutboxEvent(payment.getId(), event.eventType(), event);
    }

    private void createPaymentRefundedOutboxEvent(Payment payment, String correlationId, String causationId, String reason) {
        var event = new PaymentRefundedEventV1(
                UUID.randomUUID().toString(),
                "PaymentRefundedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                payment.getOrderId(),
                payment.getOrderId(),
                correlationId,
                causationId,
                Instant.now(),
                reason,
                payment.getId(),
                payment.getRefundTransactionId(),
                payment.getAmount(),
                payment.getCurrency()
        );
        createOutboxEvent(payment.getId(), event.eventType(), event);
    }

    private void createPaymentRefundFailedOutboxEvent(Payment payment, String correlationId, String causationId, String reason) {
        var event = new PaymentRefundFailedEventV1(
                UUID.randomUUID().toString(),
                "PaymentRefundFailedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                payment.getOrderId(),
                payment.getOrderId(),
                correlationId,
                causationId,
                Instant.now(),
                reason,
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency()
        );
        createOutboxEvent(payment.getId(), event.eventType(), event);
    }

    private void createOutboxEvent(UUID aggregateId, String eventType, Object payload) {
        try {
            outboxEventRepository.save(PaymentOutboxEvent.create(
                    "Payment",
                    aggregateId.toString(),
                    eventType,
                    RabbitTopics.EVENT_VERSION_V1,
                    objectMapper.writeValueAsString(payload)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize payment outbox event", exception);
        }
    }

    private boolean alreadyProcessed(String eventId, String consumerName) {
        return processedEventRepository.existsByEventKey(eventId + ":" + consumerName);
    }

    private void markProcessed(String eventId, String consumerName) {
        if (!alreadyProcessed(eventId, consumerName)) {
            processedEventRepository.save(ProcessedEvent.create(eventId, consumerName));
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
