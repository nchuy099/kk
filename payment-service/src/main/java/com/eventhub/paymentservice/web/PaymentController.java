package com.eventhub.paymentservice.web;

import com.eventhub.paymentservice.service.PaymentService;
import com.eventhub.paymentservice.web.dto.CreatePaymentRequest;
import com.eventhub.paymentservice.web.dto.PaymentResponse;
import com.eventhub.paymentservice.web.dto.PaymentWebhookRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/payments/{paymentId}")
    public PaymentResponse get(@PathVariable UUID paymentId) {
        return paymentService.get(paymentId);
    }

    @GetMapping("/payments/by-order/{orderId}")
    public PaymentResponse getByOrderId(@PathVariable UUID orderId) {
        return paymentService.getByOrderId(orderId);
    }

    @PostMapping("/payments/{paymentId}/mock-success")
    public PaymentResponse mockSuccess(@PathVariable UUID paymentId) {
        return paymentService.mockSuccess(paymentId);
    }

    @PostMapping("/payments/webhook")
    public PaymentResponse webhook(@Valid @RequestBody PaymentWebhookRequest request) {
        return paymentService.handleWebhook(request);
    }
}
