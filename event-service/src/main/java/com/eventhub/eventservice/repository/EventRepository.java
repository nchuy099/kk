package com.eventhub.eventservice.repository;

import com.eventhub.eventservice.domain.Event;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {
}

