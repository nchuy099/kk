package com.eventhub.eventservice.service;

import com.eventhub.eventservice.domain.Event;
import com.eventhub.eventservice.domain.TicketType;
import com.eventhub.eventservice.domain.TicketTypeStatus;
import com.eventhub.eventservice.client.InventoryServiceClient;
import com.eventhub.eventservice.repository.EventRepository;
import com.eventhub.eventservice.repository.TicketTypeRepository;
import com.eventhub.eventservice.service.exception.NotFoundException;
import com.eventhub.eventservice.web.dto.CreateEventRequest;
import com.eventhub.eventservice.web.dto.CreateTicketTypeRequest;
import com.eventhub.eventservice.web.dto.EventResponse;
import com.eventhub.eventservice.web.dto.TicketTypeResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public EventService(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            InventoryServiceClient inventoryServiceClient
    ) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public Page<EventResponse> list(Pageable pageable) {
        return eventRepository.findAll(pageable).map(this::toResponse);
    }

    public EventResponse get(UUID id) {
        return toResponse(findEvent(id));
    }

    public TicketTypeResponse getTicketType(UUID id) {
        return toTicketTypeResponse(ticketTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket type not found: " + id)));
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        var event = Event.create(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                request.startTime(),
                request.saleStartTime(),
                request.saleEndTime(),
                com.eventhub.eventservice.domain.EventStatus.PUBLISHED,
                request.venue()
        );
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public TicketTypeResponse addTicketType(UUID eventId, CreateTicketTypeRequest request) {
        var event = findEvent(eventId);
        var ticketType = TicketType.create(
                UUID.randomUUID(),
                event,
                request.name(),
                request.price(),
                request.totalQuantity(),
                TicketTypeStatus.ACTIVE
        );
        return toTicketTypeResponse(ticketTypeRepository.save(ticketType));
    }

    private Event findEvent(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found: " + id));
    }

    private EventResponse toResponse(Event event) {
        var ticketTypes = event.getTicketTypes().stream().map(this::toTicketTypeResponse).toList();
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartTime(),
                event.getSaleStartTime(),
                event.getSaleEndTime(),
                event.getStatus(),
                event.getVenue(),
                ticketTypes
        );
    }

    private TicketTypeResponse toTicketTypeResponse(TicketType ticketType) {
        var inventory = inventoryServiceClient.getInventory(ticketType.getId());
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getTotalQuantity(),
                inventory.availableQuantity(),
                ticketType.getStatus()
        );
    }
}
