package com.eventhub.orderservice.client;

import com.eventhub.orderservice.web.dto.ReservationSnapshot;
import com.eventhub.orderservice.web.dto.ReserveOrderRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
public class InventoryServiceClient {
    private final RestClient restClient;

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveFallback")
    public ReservationSnapshot reserve(ReserveOrderRequest request) {
        return restClient.post()
                .uri("/reservations")
                .body(request)
                .retrieve()
                .body(ReservationSnapshot.class);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "releaseFallback")
    public void release(String reservationId) {
        restClient.post()
                .uri("/reservations/{id}/release", reservationId)
                .retrieve()
                .toBodilessEntity();
    }

    private ReservationSnapshot reserveFallback(ReserveOrderRequest request, Throwable exception) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "inventory-service unavailable", exception);
    }

    private void releaseFallback(String reservationId, Throwable exception) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "inventory-service unavailable", exception);
    }
}
