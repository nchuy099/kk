package com.eventhub.ticketservice.repository;

import com.eventhub.ticketservice.domain.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    boolean existsByEventKey(String eventKey);
}
