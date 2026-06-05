package com.eventhub.eventservice;

import com.eventhub.common.web.CommonRequestLoggingConfig;
import com.eventhub.eventservice.domain.Competition;
import com.eventhub.eventservice.domain.CompetitionStatus;
import com.eventhub.eventservice.domain.Event;
import com.eventhub.eventservice.domain.EventStatus;
import com.eventhub.eventservice.domain.SportType;
import com.eventhub.eventservice.domain.Stadium;
import com.eventhub.eventservice.domain.TicketType;
import com.eventhub.eventservice.domain.TicketTypeStatus;
import com.eventhub.eventservice.repository.CompetitionRepository;
import com.eventhub.eventservice.repository.EventRepository;
import com.eventhub.eventservice.repository.TicketTypeRepository;
import com.eventhub.eventservice.repository.StadiumRepository;
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

@SpringBootApplication
@Import(CommonRequestLoggingConfig.class)
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(
            CompetitionRepository competitionRepository,
            StadiumRepository stadiumRepository,
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            @Value("${app.demo.competition-id}") UUID competitionId,
            @Value("${app.demo.stadium-id}") UUID stadiumId,
            @Value("${app.demo.event-id}") UUID eventId,
            @Value("${app.demo.category1-ticket-category-id}") UUID category1TicketCategoryId,
            @Value("${app.demo.category2-ticket-category-id}") UUID category2TicketCategoryId
    ) {
        return args -> {
            var competition = competitionRepository.findById(competitionId).orElseGet(() ->
                    competitionRepository.save(Competition.create(
                            competitionId,
                            "World Cup 2026",
                            SportType.FOOTBALL,
                            OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                            OffsetDateTime.of(2026, 7, 31, 23, 59, 59, 0, ZoneOffset.UTC).toInstant(),
                            CompetitionStatus.PUBLISHED
                    )));
            var stadium = stadiumRepository.findById(stadiumId).orElseGet(() ->
                    stadiumRepository.save(Stadium.create(
                            stadiumId,
                            "MetLife Stadium",
                            "East Rutherford",
                            "USA",
                            82500,
                            "One MetLife Stadium Dr"
                    )));
            var event = eventRepository.findById(eventId).orElseGet(() -> {
                var created = Event.create(
                        eventId,
                        "Argentina vs France",
                        "World Cup 2026 group stage match.",
                        competition,
                        stadium,
                        "Argentina",
                        "France",
                        OffsetDateTime.of(2026, 7, 1, 19, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                        Instant.now().minusSeconds(3600),
                        Instant.now().plusSeconds(7 * 24 * 3600),
                        EventStatus.PUBLISHED
                );
                return eventRepository.save(created);
            });
            createTicketType(ticketTypeRepository, event, category1TicketCategoryId, "Category 1", "Stand A", new BigDecimal("500000"), "USD", 100);
            createTicketType(ticketTypeRepository, event, category2TicketCategoryId, "Category 2", "Stand B", new BigDecimal("200000"), "USD", 500);
        };
    }

    private static void createTicketType(
            TicketTypeRepository ticketTypeRepository,
            Event event,
            UUID id,
            String name,
            String sectionName,
            BigDecimal price,
            String currency,
            int quantity
    ) {
        if (ticketTypeRepository.existsById(id)) {
            return;
        }
        var ticketType = TicketType.create(id, event, name, sectionName, price, currency, quantity, TicketTypeStatus.ACTIVE);
        ticketTypeRepository.save(ticketType);
    }
}
