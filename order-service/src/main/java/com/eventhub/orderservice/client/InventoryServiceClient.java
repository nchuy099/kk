package com.eventhub.orderservice.client;

import com.eventhub.orderservice.web.dto.ReservationSnapshot;
import com.eventhub.orderservice.web.dto.ReserveOrderRequest;
import org.springframework.web.client.RestClient;

public class InventoryServiceClient {
    private final RestClient restClient;

    public InventoryServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ReservationSnapshot reserve(ReserveOrderRequest request) {
        return restClient.post()
                .uri("/reservations")
                .body(request)
                .retrieve()
                .body(ReservationSnapshot.class);
    }

    public void release(String reservationId) {
        restClient.post()
                .uri("/reservations/{id}/release", reservationId)
                .retrieve()
                .toBodilessEntity();
    }
}

