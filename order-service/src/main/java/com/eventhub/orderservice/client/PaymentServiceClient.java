package com.eventhub.orderservice.client;

import com.eventhub.orderservice.web.dto.CreatePaymentRequest;
import com.eventhub.orderservice.web.dto.PaymentSnapshot;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PaymentServiceClient {
    private final RestClient restClient;

    public PaymentServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retry(name = "paymentService")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentFallback")
    public PaymentSnapshot createPayment(CreatePaymentRequest request) {
        return restClient.post()
                .uri("/payments")
                .body(request)
                .retrieve()
                .body(PaymentSnapshot.class);
    }

    private PaymentSnapshot createPaymentFallback(CreatePaymentRequest request, Throwable exception) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "payment-service unavailable", exception);
    }
}
