package com.eventhub.eventservice.repository;

import com.eventhub.eventservice.domain.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByCompetitionId(UUID competitionId);
}
