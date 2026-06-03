package com.eventhub.eventservice.client;

import com.eventhub.eventservice.service.exception.NotFoundException;
import com.eventhub.eventservice.web.dto.TicketInventorySnapshot;
import java.util.UUID;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class InventoryServiceClient {
    private final RestClient restClient;

    public InventoryServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

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
}
