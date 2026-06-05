package com.eventhub.inventoryservice.repository;

import com.eventhub.inventoryservice.domain.InventoryOutboxEvent;
import com.eventhub.inventoryservice.domain.InventoryOutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryOutboxEventRepository extends JpaRepository<InventoryOutboxEvent, UUID> {
    List<InventoryOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(InventoryOutboxEventStatus status);
}
