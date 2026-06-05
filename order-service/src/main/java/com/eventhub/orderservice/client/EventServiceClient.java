package com.eventhub.orderservice.client;

import com.eventhub.orderservice.service.exception.NotFoundException;
import com.eventhub.orderservice.web.dto.TicketTypeSnapshot;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

public class EventServiceClient {
    private final RestClient restClient;

    public EventServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retry(name = "eventService")
    @CircuitBreaker(name = "eventService", fallbackMethod = "getTicketTypeFallback")
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

    private TicketTypeSnapshot getTicketTypeFallback(UUID ticketTypeId, Throwable exception) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "event-service unavailable", exception);
    }
}
