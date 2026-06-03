package com.eventhub.eventservice.repository;

import com.eventhub.eventservice.domain.TicketType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {
    Optional<TicketType> findByIdAndEvent_Id(UUID id, UUID eventId);
}

