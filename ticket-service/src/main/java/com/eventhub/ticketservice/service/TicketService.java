package com.eventhub.ticketservice.service;

import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderRefundedEventV1;
import com.eventhub.common.events.v1.TicketIssueFailedEventV1;
import com.eventhub.common.events.v1.TicketIssueRequestedEventV1;
import com.eventhub.common.events.v1.TicketIssuedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.ticketservice.domain.ProcessedEvent;
import com.eventhub.ticketservice.domain.Ticket;
import com.eventhub.ticketservice.domain.TicketIssuance;
import com.eventhub.ticketservice.domain.TicketOutboxEvent;
import com.eventhub.ticketservice.domain.TicketStatus;
import com.eventhub.ticketservice.repository.ProcessedEventRepository;
import com.eventhub.ticketservice.repository.TicketIssuanceRepository;
import com.eventhub.ticketservice.repository.TicketOutboxEventRepository;
import com.eventhub.ticketservice.repository.TicketRepository;
import com.eventhub.ticketservice.service.exception.NotFoundException;
import com.eventhub.ticketservice.service.exception.TicketConflictException;
import com.eventhub.ticketservice.web.dto.TicketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketIssuanceRepository ticketIssuanceRepository;
    private final TicketOutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;
    private final String simulateTicketIssueFailurePrefix;

    public TicketService(
            TicketRepository ticketRepository,
            TicketIssuanceRepository ticketIssuanceRepository,
            TicketOutboxEventRepository outboxEventRepository,
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper,
            Tracer tracer,
            @Value("${app.simulate-ticket-issue-failure-prefix:fail-ticket}") String simulateTicketIssueFailurePrefix
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketIssuanceRepository = ticketIssuanceRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
        this.simulateTicketIssueFailurePrefix = simulateTicketIssueFailurePrefix;
    }

    @Transactional
    public void issueTickets(TicketIssueRequestedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "ticketIssueRequested")) {
            return;
        }
        if (ticketIssuanceRepository.existsByOrderId(event.orderId())) {
            markProcessed(event.eventId(), "ticketIssueRequested");
            return;
        }

        try {
            ticketIssuanceRepository.save(TicketIssuance.create(event.orderId()));
        } catch (DataIntegrityViolationException exception) {
            markProcessed(event.eventId(), "ticketIssueRequested");
            return;
        }

        if (event.userId().startsWith(simulateTicketIssueFailurePrefix)) {
            createOutboxEvent(event.orderId(), "TicketIssueFailedEvent", new TicketIssueFailedEventV1(
                    UUID.randomUUID().toString(),
                    "TicketIssueFailedEvent",
                    RabbitTopics.EVENT_VERSION_V1,
                    event.sagaId(),
                    event.orderId(),
                    event.correlationId(),
                    event.eventId(),
                    Instant.now(),
                    "Ticket issuance failed in simulation",
                    event.userId(),
                    event.paymentId()
            ));
            markProcessed(event.eventId(), "ticketIssueRequested");
            return;
        }

        var issuedCodes = new ArrayList<String>();
        for (int index = 1; index <= event.quantity(); index++) {
            var ticketCode = buildTicketCode(event.orderId(), index);
            issuedCodes.add(ticketCode);
            ticketRepository.save(Ticket.create(event.orderId(), event.userId(), event.ticketCategoryId(), ticketCode));
        }

        createOutboxEvent(event.orderId(), "TicketIssuedEvent", new TicketIssuedEventV1(
                UUID.randomUUID().toString(),
                "TicketIssuedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                event.sagaId(),
                event.orderId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                null,
                event.userId(),
                issuedCodes
        ));
        markProcessed(event.eventId(), "ticketIssueRequested");
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
    public void handleOrderRefunded(OrderRefundedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderRefunded")) {
            return;
        }
        cancelTickets(event.orderId());
        markProcessed(event.eventId(), "orderRefunded");
    }

    @Transactional
    public void handleOrderCancelled(OrderCancelledEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderCancelled")) {
            return;
        }
        cancelTickets(event.orderId());
        markProcessed(event.eventId(), "orderCancelled");
    }

    private void cancelTickets(UUID orderId) {
        for (var ticket : ticketRepository.findByOrderId(orderId)) {
            if (ticket.getStatus() != TicketStatus.CANCELLED && ticket.getStatus() != TicketStatus.CHECKED_IN) {
                ticket.cancel();
                ticketRepository.save(ticket);
            }
        }
    }

    private boolean alreadyProcessed(String eventId, String consumerName) {
        return processedEventRepository.existsByEventKey(eventId + ":" + consumerName);
    }

    private void markProcessed(String eventId, String consumerName) {
        if (!alreadyProcessed(eventId, consumerName)) {
            processedEventRepository.save(ProcessedEvent.create(eventId, consumerName));
        }
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

    private void createOutboxEvent(UUID aggregateId, String eventType, Object payload) {
        try {
            outboxEventRepository.save(TicketOutboxEvent.create(
                    "Ticket",
                    aggregateId.toString(),
                    eventType,
                    RabbitTopics.EVENT_VERSION_V1,
                    objectMapper.writeValueAsString(payload)
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
