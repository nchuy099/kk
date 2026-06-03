package com.eventhub.ticketservice.repository;

import com.eventhub.ticketservice.domain.Ticket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByUserId(String userId);

    Optional<Ticket> findByTicketCode(String ticketCode);

    boolean existsByOrderId(UUID orderId);

    List<Ticket> findByOrderId(UUID orderId);
}

