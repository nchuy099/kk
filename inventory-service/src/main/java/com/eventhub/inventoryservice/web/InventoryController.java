package com.eventhub.inventoryservice.web;

import com.eventhub.inventoryservice.service.InventoryService;
import com.eventhub.inventoryservice.web.dto.ReservationResponse;
import com.eventhub.inventoryservice.web.dto.ReserveRequest;
import com.eventhub.inventoryservice.web.dto.TicketInventoryResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reservations")
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.reserve(request);
    }

    @GetMapping("/reservations/{id}")
    public ReservationResponse getReservation(@PathVariable UUID id) {
        return inventoryService.getReservation(id);
    }

    @PostMapping("/reservations/{id}/confirm")
    public ReservationResponse confirm(@PathVariable UUID id) {
        return inventoryService.confirm(id);
    }

    @PostMapping("/reservations/{id}/release")
    public ReservationResponse release(@PathVariable UUID id) {
        return inventoryService.release(id);
    }

    @GetMapping("/inventories/{ticketTypeId}")
    public TicketInventoryResponse getInventory(@PathVariable UUID ticketTypeId) {
        return inventoryService.getInventory(ticketTypeId);
    }
}

