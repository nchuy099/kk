package com.eventhub.eventservice.client;

import com.eventhub.eventservice.service.exception.NotFoundException;
import com.eventhub.eventservice.web.dto.TicketInventorySnapshot;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
public class InventoryServiceClient {
    private final RestClient restClient;

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getInventoryFallback")
    public TicketInventorySnapshot getInventory(UUID ticketCategoryId) {
        try {
            return restClient.get()
                    .uri("/inventories/{ticketCategoryId}", ticketCategoryId)
                    .retrieve()
                    .body(TicketInventorySnapshot.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new NotFoundException("Inventory not found for ticket category: " + ticketCategoryId);
            }
            throw exception;
        }
    }

    private TicketInventorySnapshot getInventoryFallback(UUID ticketCategoryId, Throwable exception) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "inventory-service unavailable", exception);
    }
}
