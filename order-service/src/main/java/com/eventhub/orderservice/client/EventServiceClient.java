package com.eventhub.orderservice.client;

import com.eventhub.orderservice.service.exception.NotFoundException;
import com.eventhub.orderservice.web.dto.TicketTypeSnapshot;
import java.util.UUID;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

public class EventServiceClient {
    private final RestClient restClient;

    public EventServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public TicketTypeSnapshot getTicketType(UUID ticketTypeId) {
        try {
            return restClient.get()
                    .uri("/ticket-types/{id}", ticketTypeId)
                    .retrieve()
                    .body(TicketTypeSnapshot.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new NotFoundException("Ticket type not found: " + ticketTypeId);
            }
            throw exception;
        }
    }
}
