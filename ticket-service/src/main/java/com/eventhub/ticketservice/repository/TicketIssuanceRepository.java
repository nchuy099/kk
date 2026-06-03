package com.eventhub.ticketservice.repository;

import com.eventhub.ticketservice.domain.TicketIssuance;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketIssuanceRepository extends JpaRepository<TicketIssuance, UUID> {
    boolean existsByOrderId(UUID orderId);
}

