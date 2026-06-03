package com.eventhub.inventoryservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

@Entity
@Table(name = "ticket_inventory")
public class TicketInventory {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID ticketTypeId;

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

    protected TicketInventory() {
    }

    public static TicketInventory create(UUID ticketTypeId, int totalQuantity) {
        var inventory = new TicketInventory();
        inventory.id = UUID.randomUUID();
        inventory.ticketTypeId = ticketTypeId;
        inventory.totalQuantity = totalQuantity;
        inventory.availableQuantity = totalQuantity;
        inventory.reservedQuantity = 0;
        inventory.soldQuantity = 0;
        return inventory;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTicketTypeId() {
        return ticketTypeId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public long getVersion() {
        return version;
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
}
