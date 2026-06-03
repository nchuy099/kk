package com.eventhub.ticketservice.service;

import com.eventhub.common.events.OrderPaidEvent;
import com.eventhub.common.events.TicketIssuedEvent;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.ticketservice.domain.Ticket;
import com.eventhub.ticketservice.domain.TicketIssuance;
import com.eventhub.ticketservice.domain.TicketStatus;
import com.eventhub.ticketservice.repository.TicketRepository;
import com.eventhub.ticketservice.repository.TicketIssuanceRepository;
import com.eventhub.ticketservice.service.exception.NotFoundException;
import com.eventhub.ticketservice.service.exception.TicketConflictException;
import com.eventhub.ticketservice.web.dto.TicketResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketIssuanceRepository ticketIssuanceRepository;
    private final RabbitTemplate rabbitTemplate;

    public TicketService(
            TicketRepository ticketRepository,
            TicketIssuanceRepository ticketIssuanceRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketIssuanceRepository = ticketIssuanceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void issueTickets(OrderPaidEvent event) {
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
            ticketRepository.save(Ticket.create(event.orderId(), event.userId(), event.ticketTypeId(), ticketCode));
        }

        rabbitTemplate.convertAndSend(
                RabbitTopics.TICKET_EXCHANGE,
                RabbitTopics.TICKET_ISSUED_ROUTING_KEY,
                new TicketIssuedEvent(event.orderId(), event.userId(), issuedCodes, java.time.Instant.now())
        );
    }

    public List<TicketResponse> getTicketsForUser(String userId) {
        return ticketRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public TicketResponse getByTicketCode(String ticketCode) {
        return toResponse(findTicketByCode(ticketCode));
    }

    @Transactional
    public TicketResponse checkIn(String ticketCode) {
        var ticket = findTicketByCode(ticketCode);
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new TicketConflictException("Ticket already checked in");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new TicketConflictException("Ticket is cancelled");
        }
        ticket.markUsed();
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public void handleOrderPaid(OrderPaidEvent event) {
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
                ticket.getTicketTypeId(),
                ticket.getTicketCode(),
                ticket.getQrCodeUrl(),
                ticket.getStatus(),
                ticket.getIssuedAt()
        );
    }

    private static String buildTicketCode(UUID orderId, int index) {
        var compactOrderId = orderId.toString().substring(0, 8).toUpperCase();
        return "EVT-" + compactOrderId + "-" + index + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
