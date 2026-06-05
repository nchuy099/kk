package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.ticketservice.domain.Ticket;
import com.eventhub.ticketservice.domain.TicketIssuance;
import com.eventhub.ticketservice.domain.TicketOutboxEvent;
import com.eventhub.ticketservice.domain.TicketStatus;
import com.eventhub.ticketservice.repository.TicketRepository;
import com.eventhub.ticketservice.repository.TicketIssuanceRepository;
import com.eventhub.ticketservice.repository.TicketOutboxEventRepository;
import com.eventhub.ticketservice.service.exception.NotFoundException;
import com.eventhub.ticketservice.service.exception.TicketConflictException;
import com.eventhub.ticketservice.web.dto.TicketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
    @Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketIssuanceRepository ticketIssuanceRepository;
    private final TicketOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public TicketService(
            TicketRepository ticketRepository,
            TicketIssuanceRepository ticketIssuanceRepository,
            TicketOutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            Tracer tracer
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketIssuanceRepository = ticketIssuanceRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Transactional
    public void issueTickets(OrderConfirmedEventV1 event) {
        if (ticketIssuanceRepository.existsByOrderId(event.orderId())) {
            return;
        }

        try {
            ticketIssuanceRepository.save(TicketIssuance.create(event.orderId()));
        } catch (DataIntegrityViolationException exception) {
            return;
        }

        var issuedCodes = new ArrayList<String>();
        for (int index = 1; index <= event.quantity(); index++) {
            var ticketCode = buildTicketCode(event.orderId(), index);
            issuedCodes.add(ticketCode);
            ticketRepository.save(Ticket.create(event.orderId(), event.userId(), event.ticketCategoryId(), ticketCode));
        }

        createTicketIssuedOutboxEvent(event, issuedCodes);
    }

    public List<TicketResponse> getTicketsForUser(String userId) {
        return ticketRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public List<TicketResponse> getTicketsForOrder(UUID orderId) {
        return ticketRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
    }

    public TicketResponse getByTicketCode(String ticketCode) {
        return toResponse(findTicketByCode(ticketCode));
    }

    @Transactional
    public TicketResponse checkIn(String ticketCode) {
        var ticket = findTicketByCode(ticketCode);
        if (ticket.getStatus() == TicketStatus.CHECKED_IN) {
            throw new TicketConflictException("Ticket already checked in");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new TicketConflictException("Ticket is cancelled");
        }
        ticket.markUsed();
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEventV1 event) {
        issueTickets(event);
    }

    private Ticket findTicketByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketCode));
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getOrderId(),
                ticket.getUserId(),
                ticket.getTicketCategoryId(),
                ticket.getTicketCode(),
                ticket.getQrCodePayload(),
                ticket.getStatus(),
                ticket.getIssuedAt()
        );
    }

    private static String buildTicketCode(UUID orderId, int index) {
        var compactOrderId = orderId.toString().substring(0, 8).toUpperCase();
        return "EVT-" + compactOrderId + "-" + index + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void createTicketIssuedOutboxEvent(OrderConfirmedEventV1 orderConfirmedEvent, List<String> issuedCodes) {
        var event = new TicketIssuedEventV1(
                UUID.randomUUID().toString(),
                "TicketIssuedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                currentCorrelationId(),
                java.time.Instant.now(),
                orderConfirmedEvent.orderId(),
                orderConfirmedEvent.userId(),
                issuedCodes
        );
        try {
            outboxEventRepository.save(TicketOutboxEvent.create(
                    "Ticket",
                    orderConfirmedEvent.orderId().toString(),
                    event.eventType(),
                    event.eventVersion(),
                    objectMapper.writeValueAsString(event)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize ticket outbox event", exception);
        }
    }

    private String currentCorrelationId() {
        var span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return span.context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
