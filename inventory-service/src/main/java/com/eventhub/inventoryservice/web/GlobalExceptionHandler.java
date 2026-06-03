package com.eventhub.inventoryservice.web;

import com.eventhub.inventoryservice.service.exception.NotFoundException;
import com.eventhub.inventoryservice.service.exception.ReservationConflictException;
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

    @ExceptionHandler(ReservationConflictException.class)
    ResponseEntity<Map<String, Object>> handleConflict(ReservationConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 409,
                "error", exception.getMessage()
        ));
    }
}

