package com.eventhub.eventservice.service;

import com.eventhub.eventservice.domain.Competition;
import com.eventhub.eventservice.domain.CompetitionStatus;
import com.eventhub.eventservice.domain.Event;
import com.eventhub.eventservice.domain.EventStatus;
import com.eventhub.eventservice.domain.SportType;
import com.eventhub.eventservice.domain.Stadium;
import com.eventhub.eventservice.domain.TicketType;
import com.eventhub.eventservice.domain.TicketTypeStatus;
import com.eventhub.eventservice.client.InventoryServiceClient;
import com.eventhub.eventservice.repository.CompetitionRepository;
import com.eventhub.eventservice.repository.EventRepository;
import com.eventhub.eventservice.repository.TicketTypeRepository;
import com.eventhub.eventservice.repository.StadiumRepository;
import com.eventhub.eventservice.service.exception.NotFoundException;
import com.eventhub.eventservice.web.dto.CompetitionResponse;
import com.eventhub.eventservice.web.dto.CreateCompetitionRequest;
import com.eventhub.eventservice.web.dto.CreateEventRequest;
import com.eventhub.eventservice.web.dto.CreateTicketTypeRequest;
import com.eventhub.eventservice.web.dto.CreateStadiumRequest;
import com.eventhub.eventservice.web.dto.StadiumResponse;
import com.eventhub.eventservice.web.dto.EventResponse;
import com.eventhub.eventservice.web.dto.TicketTypeResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {
    private final CompetitionRepository competitionRepository;
    private final StadiumRepository stadiumRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public Page<CompetitionResponse> listCompetitions(Pageable pageable) {
        return competitionRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<EventResponse> listCompetitionEvents(UUID competitionId, Pageable pageable) {
        var events = eventRepository.findByCompetitionId(competitionId).stream().map(this::toResponse).toList();
        return new PageImpl<>(events, pageable, events.size());
    }

    public Page<EventResponse> list(Pageable pageable) {
        return eventRepository.findAll(pageable).map(this::toResponse);
    }

    public EventResponse get(UUID id) {
        return toResponse(findEvent(id));
    }

    public TicketTypeResponse getTicketCategory(UUID id) {
        return toTicketTypeResponse(ticketTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket category not found: " + id)));
    }

    @Transactional
    public CompetitionResponse createCompetition(CreateCompetitionRequest request) {
        var competition = Competition.create(
                UUID.randomUUID(),
                request.name(),
                request.sportType(),
                request.startDate(),
                request.endDate(),
                CompetitionStatus.PUBLISHED
        );
        return toResponse(competitionRepository.save(competition));
    }

    @Transactional
    public StadiumResponse createStadium(CreateStadiumRequest request) {
        var stadium = Stadium.create(
                UUID.randomUUID(),
                request.name(),
                request.city(),
                request.country(),
                request.capacity(),
                request.address()
        );
        return toResponse(stadiumRepository.save(stadium));
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        var competition = competitionRepository.findById(request.competitionId())
                .orElseThrow(() -> new NotFoundException("Competition not found: " + request.competitionId()));
        var stadium = stadiumRepository.findById(request.stadiumId())
                .orElseThrow(() -> new NotFoundException("Stadium not found: " + request.stadiumId()));
        var event = Event.create(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                competition,
                stadium,
                request.homeTeam(),
                request.awayTeam(),
                request.startTime(),
                request.saleStartTime(),
                request.saleEndTime(),
                EventStatus.PUBLISHED
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
                request.sectionName(),
                request.price(),
                request.currency(),
                request.totalQuantity(),
                TicketTypeStatus.ACTIVE
        );
        return toTicketTypeResponse(ticketTypeRepository.save(ticketType));
    }

    public Page<TicketTypeResponse> listTicketCategories(UUID eventId, Pageable pageable) {
        return eventRepository.findById(eventId)
                .map(event -> event.getTicketTypes().stream().map(this::toTicketTypeResponse).toList())
                .map(list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size()))
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
    }

    public CompetitionResponse getCompetition(UUID id) {
        return toResponse(competitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Competition not found: " + id)));
    }

    public StadiumResponse getStadium(UUID id) {
        return toResponse(stadiumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stadium not found: " + id)));
    }

    private Event findEvent(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found: " + id));
    }

    private CompetitionResponse toResponse(Competition competition) {
        return new CompetitionResponse(
                competition.getId(),
                competition.getName(),
                competition.getSportType(),
                competition.getStartDate(),
                competition.getEndDate(),
                competition.getStatus()
        );
    }

    private StadiumResponse toResponse(Stadium stadium) {
        return new StadiumResponse(
                stadium.getId(),
                stadium.getName(),
                stadium.getCity(),
                stadium.getCountry(),
                stadium.getCapacity(),
                stadium.getAddress()
        );
    }

    private EventResponse toResponse(Event event) {
        var ticketCategories = event.getTicketTypes().stream().map(this::toTicketTypeResponse).toList();
        return new EventResponse(
                event.getId(),
                event.getCompetition().getId(),
                event.getStadium().getId(),
                event.getName(),
                event.getDescription(),
                event.getHomeTeam(),
                event.getAwayTeam(),
                event.getStartTime(),
                event.getSaleStartTime(),
                event.getSaleEndTime(),
                event.getStatus(),
                ticketCategories
        );
    }

    private TicketTypeResponse toTicketTypeResponse(TicketType ticketType) {
        var inventory = inventoryServiceClient.getInventory(ticketType.getId());
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getName(),
                ticketType.getSectionName(),
                ticketType.getPrice(),
                ticketType.getCurrency(),
                ticketType.getTotalQuantity(),
                inventory.availableQuantity(),
                ticketType.getStatus()
        );
    }
}
