package com.eventhub.inventoryservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketInventory {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID ticketCategoryId;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int availableQuantity;

    @Column(nullable = false)
    private int reservedQuantity;

    @Column(nullable = false)
    private int soldQuantity;

    @Version
    private long version;

    public static TicketInventory create(UUID ticketCategoryId, int totalQuantity) {
        var inventory = new TicketInventory();
        inventory.id = UUID.randomUUID();
        inventory.ticketCategoryId = ticketCategoryId;
        inventory.totalQuantity = totalQuantity;
        inventory.availableQuantity = totalQuantity;
        inventory.reservedQuantity = 0;
        inventory.soldQuantity = 0;
        return inventory;
    }

    public void reserve(int quantity) {
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void confirm(int quantity) {
        reservedQuantity -= quantity;
        soldQuantity += quantity;
    }

    public void release(int quantity) {
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void refund(int quantity) {
        soldQuantity -= quantity;
        availableQuantity += quantity;
    }
}
