package com.eventhub.userservice.web;

import com.eventhub.userservice.service.exception.NotFoundException;
import com.eventhub.userservice.service.exception.UserProvisioningException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, exception.getMessage()));
    }

    @ExceptionHandler(UserProvisioningException.class)
    ResponseEntity<Map<String, Object>> handleProvisioning(UserProvisioningException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error(502, exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MissingRequestHeaderException.class})
    ResponseEntity<Map<String, Object>> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest().body(error(400, "Validation failed"));
    }

    private static Map<String, Object> error(int status, String message) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status,
                "error", message
        );
    }
}
