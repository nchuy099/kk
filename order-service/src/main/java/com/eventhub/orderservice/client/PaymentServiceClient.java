package com.eventhub.orderservice.client;

import com.eventhub.orderservice.web.dto.CreatePaymentRequest;
import com.eventhub.orderservice.web.dto.PaymentSnapshot;
import org.springframework.web.client.RestClient;

public class PaymentServiceClient {
    private final RestClient restClient;

    public PaymentServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public PaymentSnapshot createPayment(CreatePaymentRequest request) {
        return restClient.post()
                .uri("/payments")
                .body(request)
                .retrieve()
                .body(PaymentSnapshot.class);
    }
}

