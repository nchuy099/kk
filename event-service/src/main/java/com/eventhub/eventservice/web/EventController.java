package com.eventhub.eventservice.web;

import com.eventhub.eventservice.service.EventService;
import com.eventhub.eventservice.web.dto.CreateEventRequest;
import com.eventhub.eventservice.web.dto.CreateTicketTypeRequest;
import com.eventhub.eventservice.web.dto.EventResponse;
import com.eventhub.eventservice.web.dto.TicketTypeResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public Page<EventResponse> list(Pageable pageable) {
        return eventService.list(pageable);
    }

    @GetMapping("/events/{id}")
    public EventResponse get(@PathVariable UUID id) {
        return eventService.get(id);
    }

    @GetMapping("/ticket-types/{id}")
    public TicketTypeResponse getTicketType(@PathVariable UUID id) {
        return eventService.getTicketType(id);
    }

    @PostMapping("/admin/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @PostMapping("/admin/events/{eventId}/ticket-types")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketTypeResponse createTicketType(@PathVariable UUID eventId, @Valid @RequestBody CreateTicketTypeRequest request) {
        return eventService.addTicketType(eventId, request);
    }
}

