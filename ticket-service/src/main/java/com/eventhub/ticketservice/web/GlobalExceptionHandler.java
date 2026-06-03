package com.eventhub.ticketservice.web;

import com.eventhub.ticketservice.service.exception.NotFoundException;
import com.eventhub.ticketservice.service.exception.TicketConflictException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 404,
                "error", exception.getMessage()
        ));
    }

    @ExceptionHandler(TicketConflictException.class)
    ResponseEntity<Map<String, Object>> handleConflict(TicketConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 409,
                "error", exception.getMessage()
        ));
    }
}

