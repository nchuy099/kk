package com.eventhub.inventoryservice.service;

import com.eventhub.common.events.OrderPaidEvent;
import com.eventhub.inventoryservice.domain.Reservation;
import com.eventhub.inventoryservice.domain.ReservationStatus;
import com.eventhub.inventoryservice.domain.TicketInventory;
import com.eventhub.inventoryservice.repository.ReservationRepository;
import com.eventhub.inventoryservice.repository.TicketInventoryRepository;
import com.eventhub.inventoryservice.service.exception.NotFoundException;
import com.eventhub.inventoryservice.service.exception.ReservationConflictException;
import com.eventhub.inventoryservice.web.dto.ReservationResponse;
import com.eventhub.inventoryservice.web.dto.ReserveRequest;
import com.eventhub.inventoryservice.web.dto.TicketInventoryResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private final TicketInventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final Duration reservationTtl;

    public InventoryService(
            TicketInventoryRepository inventoryRepository,
            ReservationRepository reservationRepository,
            @Value("${app.reservation-ttl}") Duration reservationTtl
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTtl = reservationTtl;
    }

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        var inventory = inventoryRepository.findLockedByTicketTypeId(request.ticketTypeId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket type: " + request.ticketTypeId()));

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new ReservationConflictException("Not enough available tickets");
        }

        inventory.reserve(request.quantity());
        inventoryRepository.save(inventory);

        var reservation = Reservation.create(
                request.userId(),
                request.ticketTypeId(),
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

        var inventory = inventoryRepository.findLockedByTicketTypeId(reservation.getTicketTypeId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket type: " + reservation.getTicketTypeId()));
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

        var inventory = inventoryRepository.findLockedByTicketTypeId(reservation.getTicketTypeId())
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket type: " + reservation.getTicketTypeId()));
        inventory.release(reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.markReleased();
        return toResponse(reservationRepository.save(reservation));
    }

    public TicketInventoryResponse getInventory(UUID ticketTypeId) {
        var inventory = inventoryRepository.findByTicketTypeId(ticketTypeId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for ticket type: " + ticketTypeId));
        return toResponse(inventory);
    }

    @Scheduled(fixedDelay = 30_000L)
    @Transactional
    public void expireReservations() {
        var expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, Instant.now());
        for (var reservation : expiredReservations) {
            var inventory = inventoryRepository.findLockedByTicketTypeId(reservation.getTicketTypeId())
                    .orElseThrow(() -> new NotFoundException("Inventory not found for ticket type: " + reservation.getTicketTypeId()));
            inventory.release(reservation.getQuantity());
            inventoryRepository.save(inventory);
            reservation.markExpired();
            reservationRepository.save(reservation);
        }
    }

    @Transactional
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        confirm(event.reservationId());
    }

    private Reservation findReservation(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + id));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getTicketTypeId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getOrderId()
        );
    }

    private TicketInventoryResponse toResponse(TicketInventory inventory) {
        return new TicketInventoryResponse(
                inventory.getId(),
                inventory.getTicketTypeId(),
                inventory.getTotalQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getSoldQuantity(),
                inventory.getVersion()
        );
    }
}
