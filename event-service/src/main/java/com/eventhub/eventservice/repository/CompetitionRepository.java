package com.eventhub.eventservice.repository;

import com.eventhub.eventservice.domain.Competition;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, UUID> {
}
