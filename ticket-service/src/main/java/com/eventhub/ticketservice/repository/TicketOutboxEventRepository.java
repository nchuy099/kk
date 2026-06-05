package com.eventhub.ticketservice.repository;

import com.eventhub.ticketservice.domain.TicketOutboxEvent;
import com.eventhub.ticketservice.domain.TicketOutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketOutboxEventRepository extends JpaRepository<TicketOutboxEvent, UUID> {
    List<TicketOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(TicketOutboxEventStatus status);
}
