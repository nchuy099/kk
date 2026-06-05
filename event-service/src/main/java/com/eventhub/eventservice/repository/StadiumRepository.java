package com.eventhub.eventservice.repository;

import com.eventhub.eventservice.domain.Stadium;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumRepository extends JpaRepository<Stadium, UUID> {
}
