package com.eventhub.paymentservice.service;

import com.eventhub.common.events.PaymentSucceededEvent;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.paymentservice.domain.Payment;
import com.eventhub.paymentservice.domain.PaymentStatus;
import com.eventhub.paymentservice.domain.PaymentWebhookEvent;
import com.eventhub.paymentservice.repository.PaymentRepository;
import com.eventhub.paymentservice.repository.PaymentWebhookEventRepository;
import com.eventhub.paymentservice.service.exception.NotFoundException;
import com.eventhub.paymentservice.service.exception.PaymentConflictException;
import com.eventhub.paymentservice.web.dto.CreatePaymentRequest;
import com.eventhub.paymentservice.web.dto.PaymentResponse;
import com.eventhub.paymentservice.web.dto.PaymentWebhookRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentWebhookEventRepository webhookEventRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.paymentRepository = paymentRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        var existing = paymentRepository.findByOrderId(request.orderId());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        var payment = Payment.create(request.orderId(), request.amount());
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
                "SUCCESS",
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

        if ("SUCCESS".equalsIgnoreCase(request.status())) {
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.markSuccess(request.transactionId());
                payment = paymentRepository.save(payment);
                publishPaymentSucceeded(payment);
            }
            return toResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.markFailed(request.transactionId());
            payment = paymentRepository.save(payment);
        }
        return toResponse(payment);
    }

    private void publishPaymentSucceeded(Payment payment) {
        rabbitTemplate.convertAndSend(
                RabbitTopics.PAYMENT_EXCHANGE,
                RabbitTopics.PAYMENT_SUCCESS_ROUTING_KEY,
                new PaymentSucceededEvent(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getTransactionId(),
                        payment.getAmount(),
                        Instant.now()
                )
        );
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
                payment.getStatus(),
                payment.getTransactionId()
        );
    }

    private static String requestToJson(PaymentWebhookRequest request) {
        return "{\"providerEventId\":\"" + request.providerEventId() + "\",\"transactionId\":\"" + request.transactionId() + "\",\"orderId\":\"" + request.orderId() + "\",\"status\":\"" + request.status() + "\",\"amount\":\"" + request.amount() + "\"}";
    }
}
