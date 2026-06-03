package com.eventhub.inventoryservice;

import com.eventhub.inventoryservice.domain.TicketInventory;
import com.eventhub.inventoryservice.repository.TicketInventoryRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedInventory(
            TicketInventoryRepository inventoryRepository,
            @Value("${app.demo.vip-ticket-type-id}") UUID vipTicketTypeId,
            @Value("${app.demo.ga-ticket-type-id}") UUID gaTicketTypeId
    ) {
        return args -> {
            if (inventoryRepository.findByTicketTypeId(vipTicketTypeId).isEmpty()) {
                inventoryRepository.save(TicketInventory.create(vipTicketTypeId, 100));
            }
            if (inventoryRepository.findByTicketTypeId(gaTicketTypeId).isEmpty()) {
                inventoryRepository.save(TicketInventory.create(gaTicketTypeId, 500));
            }
        };
    }
}

