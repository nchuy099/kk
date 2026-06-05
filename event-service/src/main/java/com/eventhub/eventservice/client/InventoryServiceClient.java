package com.eventhub.eventservice.client;

import com.eventhub.eventservice.service.exception.NotFoundException;
import com.eventhub.eventservice.web.dto.TicketInventorySnapshot;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InventoryServiceClient {
    private final RestClient restClient;

    public InventoryServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getInventoryFallback")
    public TicketInventorySnapshot getInventory(UUID ticketTypeId) {
        try {
            return restClient.get()
                    .uri("/inventories/{ticketTypeId}", ticketTypeId)
                    .retrieve()
                    .body(TicketInventorySnapshot.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new NotFoundException("Inventory not found for ticket type: " + ticketTypeId);
            }
            throw exception;
        }
    }

    private TicketInventorySnapshot getInventoryFallback(UUID ticketTypeId, Throwable exception) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "inventory-service unavailable", exception);
    }
}
