package com.eventhub.ticketservice.web;

import com.eventhub.ticketservice.service.TicketService;
import com.eventhub.ticketservice.web.dto.TicketResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets/my")
    public List<TicketResponse> myTickets(@RequestHeader("X-User-Id") String userId) {
        return ticketService.getTicketsForUser(userId);
    }

    @GetMapping("/orders/{orderId}/tickets")
    public List<TicketResponse> getOrderTickets(@PathVariable UUID orderId) {
        return ticketService.getTicketsForOrder(orderId);
    }

    @GetMapping("/tickets/{ticketCode}")
    public TicketResponse getByCode(@PathVariable String ticketCode) {
        return ticketService.getByTicketCode(ticketCode);
    }

    @PostMapping("/tickets/{ticketCode}/check-in")
    public TicketResponse checkIn(@PathVariable String ticketCode) {
        return ticketService.checkIn(ticketCode);
    }
}
