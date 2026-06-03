package com.eventhub.inventoryservice.repository;

import com.eventhub.inventoryservice.domain.TicketInventory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface TicketInventoryRepository extends JpaRepository<TicketInventory, UUID> {
    Optional<TicketInventory> findByTicketTypeId(UUID ticketTypeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketInventory> findLockedByTicketTypeId(UUID ticketTypeId);
}

