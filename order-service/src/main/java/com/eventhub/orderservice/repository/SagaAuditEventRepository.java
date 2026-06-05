package com.eventhub.orderservice.repository;

import com.eventhub.orderservice.domain.SagaAuditEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaAuditEventRepository extends JpaRepository<SagaAuditEvent, UUID> {
}
