package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.v1.InventoryReleaseFailedEventV1;
import com.eventhub.common.events.v1.InventoryReleasedEventV1;
import com.eventhub.common.events.v1.InventoryReserveFailedEventV1;
import com.eventhub.common.events.v1.InventoryReservedEventV1;
import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderConfirmedEventV1;
import com.eventhub.common.events.v1.OrderCreatedEventV1;
import com.eventhub.common.events.v1.OrderExpiredEventV1;
import com.eventhub.common.events.v1.OrderRefundedEventV1;
import com.eventhub.common.events.v1.ReservationExpiredEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import com.eventhub.inventoryservice.domain.InventoryOutboxEvent;
import com.eventhub.inventoryservice.domain.ProcessedEvent;
import com.eventhub.inventoryservice.domain.Reservation;
import com.eventhub.inventoryservice.domain.ReservationStatus;
import com.eventhub.inventoryservice.domain.TicketInventory;
import com.eventhub.inventoryservice.repository.InventoryOutboxEventRepository;
import com.eventhub.inventoryservice.repository.ProcessedEventRepository;
import com.eventhub.inventoryservice.repository.ReservationRepository;
import com.eventhub.inventoryservice.repository.TicketInventoryRepository;
import com.eventhub.inventoryservice.service.exception.NotFoundException;
import com.eventhub.inventoryservice.service.exception.ReservationConflictException;
import com.eventhub.inventoryservice.web.dto.ReservationResponse;
import com.eventhub.inventoryservice.web.dto.ReserveRequest;
import com.eventhub.inventoryservice.web.dto.TicketInventoryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private final TicketInventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final InventoryOutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final Duration reservationTtl;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public InventoryService(
            TicketInventoryRepository inventoryRepository,
            ReservationRepository reservationRepository,
            InventoryOutboxEventRepository outboxEventRepository,
            ProcessedEventRepository processedEventRepository,
            @Value("${app.reservation-ttl}") Duration reservationTtl,
            ObjectMapper objectMapper,
            Tracer tracer
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
        this.reservationTtl = reservationTtl;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        var inventory = inventoryRepository.findLockedByTicketCategoryId(request.ticketCategoryId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + request.ticketCategoryId()));

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new ReservationConflictException("Not enough available tickets");
        }

        inventory.reserve(request.quantity());
        inventoryRepository.save(inventory);

        var reservation = Reservation.create(
                request.userId(),
                request.ticketCategoryId(),
                request.quantity(),
                request.orderId(),
                Instant.now().plus(reservationTtl)
        );
        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse getReservation(UUID id) {
        return toResponse(findReservation(id));
    }

    @Transactional
    public ReservationResponse confirm(UUID id) {
        var reservation = findReservation(id);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return toResponse(reservation);
        }
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            return toResponse(reservation);
        }

        var inventory = inventoryRepository.findLockedByTicketCategoryId(reservation.getTicketCategoryId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + reservation.getTicketCategoryId()));
        inventory.confirm(reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.markConfirmed();
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse release(UUID id) {
        var reservation = findReservation(id);
        if (reservation.getStatus() == ReservationStatus.RELEASED || reservation.getStatus() == ReservationStatus.EXPIRED) {
            return toResponse(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return toResponse(reservation);
        }

        var inventory = inventoryRepository.findLockedByTicketCategoryId(reservation.getTicketCategoryId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + reservation.getTicketCategoryId()));
        inventory.release(reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.markReleased();
        return toResponse(reservationRepository.save(reservation));
    }

    public TicketInventoryResponse getInventory(UUID ticketCategoryId) {
        var inventory = inventoryRepository.findByTicketCategoryId(ticketCategoryId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + ticketCategoryId));
        return toResponse(inventory);
    }

    @Scheduled(fixedDelay = 30_000L)
    @Transactional
    public void expireReservations() {
        var expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, Instant.now());
        for (var reservation : expiredReservations) {
            var inventory = inventoryRepository.findLockedByTicketCategoryId(reservation.getTicketCategoryId())
                    .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + reservation.getTicketCategoryId()));
            inventory.release(reservation.getQuantity());
            inventoryRepository.save(inventory);
            reservation.markExpired();
            reservationRepository.save(reservation);
            createOutboxEvent(reservation.getOrderId(), "ReservationExpiredEvent", new ReservationExpiredEventV1(
                    UUID.randomUUID().toString(),
                    "ReservationExpiredEvent",
                    RabbitTopics.EVENT_VERSION_V1,
                    reservation.getOrderId(),
                    reservation.getOrderId(),
                    currentCorrelationId(),
                    null,
                    Instant.now(),
                    "Reservation expired",
                    reservation.getId(),
                    reservation.getTicketCategoryId(),
                    reservation.getQuantity()
            ));
        }
    }

    @Transactional
    public void handleOrderCreated(OrderCreatedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderCreated")) {
            return;
        }
        var existingReservation = reservationRepository.findByOrderId(event.orderId());
        if (existingReservation.isPresent()) {
            markProcessed(event.eventId(), "orderCreated");
            return;
        }

        var inventory = inventoryRepository.findLockedByTicketCategoryId(event.ticketCategoryId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + event.ticketCategoryId()));

        if (inventory.getAvailableQuantity() < event.quantity()) {
            createOutboxEvent(event.orderId(), "InventoryReserveFailedEvent", new InventoryReserveFailedEventV1(
                    UUID.randomUUID().toString(),
                    "InventoryReserveFailedEvent",
                    RabbitTopics.EVENT_VERSION_V1,
                    event.sagaId(),
                    event.orderId(),
                    event.correlationId(),
                    event.eventId(),
                    Instant.now(),
                    "Not enough tickets available",
                    event.ticketCategoryId(),
                    event.quantity()
            ));
            markProcessed(event.eventId(), "orderCreated");
            return;
        }

        inventory.reserve(event.quantity());
        inventoryRepository.save(inventory);
        var reservation = Reservation.create(event.userId(), event.ticketCategoryId(), event.quantity(), event.orderId(), event.expiresAt());
        reservationRepository.save(reservation);

        createOutboxEvent(event.orderId(), "InventoryReservedEvent", new InventoryReservedEventV1(
                UUID.randomUUID().toString(),
                "InventoryReservedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                event.sagaId(),
                event.orderId(),
                event.correlationId(),
                event.eventId(),
                Instant.now(),
                null,
                reservation.getId(),
                reservation.getTicketCategoryId(),
                reservation.getQuantity(),
                reservation.getExpiresAt()
        ));
        markProcessed(event.eventId(), "orderCreated");
    }

    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderConfirmed")) {
            return;
        }
        confirm(event.reservationId());
        markProcessed(event.eventId(), "orderConfirmed");
    }

    @Transactional
    public void handleOrderCancelled(OrderCancelledEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderCancelled")) {
            return;
        }
        releaseForSaga(event.orderId(), event.reason(), event.eventId(), event.correlationId(), false);
        markProcessed(event.eventId(), "orderCancelled");
    }

    @Transactional
    public void handleOrderExpired(OrderExpiredEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderExpired")) {
            return;
        }
        releaseForSaga(event.orderId(), event.reason(), event.eventId(), event.correlationId(), false);
        markProcessed(event.eventId(), "orderExpired");
    }

    @Transactional
    public void handleOrderRefunded(OrderRefundedEventV1 event) {
        if (alreadyProcessed(event.eventId(), "orderRefunded")) {
            return;
        }
        releaseForSaga(event.orderId(), event.reason(), event.eventId(), event.correlationId(), true);
        markProcessed(event.eventId(), "orderRefunded");
    }

    private void releaseForSaga(UUID orderId, String reason, String causationId, String correlationId, boolean allowConfirmedRelease) {
        var reservation = reservationRepository.findByOrderId(orderId).orElse(null);
        if (reservation == null) {
            return;
        }
        var inventory = inventoryRepository.findLockedByTicketCategoryId(reservation.getTicketCategoryId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket category: " + reservation.getTicketCategoryId()));

        if (reservation.getStatus() == ReservationStatus.RELEASED || reservation.getStatus() == ReservationStatus.EXPIRED) {
            return;
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            if (!allowConfirmedRelease) {
                return;
            }
            inventory.refund(reservation.getQuantity());
        } else {
            inventory.release(reservation.getQuantity());
        }
        inventoryRepository.save(inventory);
        reservation.markReleased();
        reservationRepository.save(reservation);
        createOutboxEvent(orderId, "InventoryReleasedEvent", new InventoryReleasedEventV1(
                UUID.randomUUID().toString(),
                "InventoryReleasedEvent",
                RabbitTopics.EVENT_VERSION_V1,
                orderId,
                orderId,
                correlationId,
                causationId,
                Instant.now(),
                reason,
                reservation.getId(),
                reservation.getTicketCategoryId(),
                reservation.getQuantity()
        ));
    }

    private boolean alreadyProcessed(String eventId, String consumerName) {
        return processedEventRepository.existsByEventKey(eventId + ":" + consumerName);
    }

    private void markProcessed(String eventId, String consumerName) {
        if (!alreadyProcessed(eventId, consumerName)) {
            processedEventRepository.save(ProcessedEvent.create(eventId, consumerName));
        }
    }

    private void createOutboxEvent(UUID aggregateId, String eventType, Object payload) {
        try {
            outboxEventRepository.save(InventoryOutboxEvent.create(
                    "Inventory",
                    aggregateId.toString(),
                    eventType,
                    RabbitTopics.EVENT_VERSION_V1,
                    objectMapper.writeValueAsString(payload)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize inventory outbox event", exception);
        }
    }

    private Reservation findReservation(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + id));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getTicketCategoryId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getOrderId()
        );
    }

    private TicketInventoryResponse toResponse(TicketInventory inventory) {
        return new TicketInventoryResponse(
                inventory.getId(),
                inventory.getTicketCategoryId(),
                inventory.getTotalQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getSoldQuantity(),
                inventory.getVersion()
        );
    }

    private String currentCorrelationId() {
        var span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return span.context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
