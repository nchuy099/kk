package com.eventhub.eventservice;

import com.eventhub.eventservice.domain.Event;
import com.eventhub.eventservice.domain.EventStatus;
import com.eventhub.eventservice.domain.TicketType;
import com.eventhub.eventservice.domain.TicketTypeStatus;
import com.eventhub.eventservice.repository.EventRepository;
import com.eventhub.eventservice.repository.TicketTypeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.eventhub.common.web.CommonRequestLoggingConfig;

@SpringBootApplication
@Import(CommonRequestLoggingConfig.class)
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            @Value("${app.demo.event-id}") UUID eventId,
            @Value("${app.demo.vip-ticket-type-id}") UUID vipTicketTypeId,
            @Value("${app.demo.ga-ticket-type-id}") UUID gaTicketTypeId
    ) {
        return args -> {
            var event = eventRepository.findById(eventId).orElseGet(() -> {
                var created = Event.create(
                        eventId,
                        "Spring Live 2026",
                        "Demo event for booking platform.",
                        OffsetDateTime.of(2026, 7, 1, 19, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        Instant.now().minusSeconds(3600),
                        Instant.now().plusSeconds(7 * 24 * 3600),
                        EventStatus.PUBLISHED,
                        "Saigon Convention Center"
                );
                return eventRepository.save(created);
            });
            createTicketType(ticketTypeRepository, event, vipTicketTypeId, "VIP", new BigDecimal("500000"), 100);
            createTicketType(ticketTypeRepository, event, gaTicketTypeId, "GENERAL", new BigDecimal("200000"), 500);
        };
    }

    private static void createTicketType(
            TicketTypeRepository ticketTypeRepository,
            Event event,
            UUID id,
            String name,
            BigDecimal price,
            int quantity
    ) {
        if (ticketTypeRepository.existsById(id)) {
            return;
        }
        var ticketType = TicketType.create(id, event, name, price, quantity, TicketTypeStatus.ACTIVE);
        ticketTypeRepository.save(ticketType);
    }
}
