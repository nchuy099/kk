package com.eventhub.inventoryservice.repository;

import com.eventhub.inventoryservice.domain.Reservation;
import com.eventhub.inventoryservice.domain.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByOrderId(UUID orderId);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant now);
}
