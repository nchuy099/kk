package com.eventhub.ticketservice.service.exception;

public class TicketConflictException extends RuntimeException {
    public TicketConflictException(String message) {
        super(message);
    }
}

