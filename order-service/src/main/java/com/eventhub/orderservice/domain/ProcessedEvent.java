package com.eventhub.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String eventKey;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String consumerName;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public static ProcessedEvent create(String eventId, String consumerName) {
        var processedEvent = new ProcessedEvent();
        processedEvent.id = UUID.randomUUID();
        processedEvent.eventId = eventId;
        processedEvent.consumerName = consumerName;
        processedEvent.eventKey = eventId + ":" + consumerName;
        processedEvent.processedAt = Instant.now();
        return processedEvent;
    }
}
