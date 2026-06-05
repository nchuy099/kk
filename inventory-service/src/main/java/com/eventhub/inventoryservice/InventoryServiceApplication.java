package com.eventhub.inventoryservice;

import com.eventhub.inventoryservice.domain.TicketInventory;
import com.eventhub.inventoryservice.repository.TicketInventoryRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.eventhub.common.web.CommonRequestLoggingConfig;

@SpringBootApplication
@EnableScheduling
@Import(CommonRequestLoggingConfig.class)
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedInventory(
            TicketInventoryRepository inventoryRepository,
            @Value("${app.demo.category1-ticket-category-id}") UUID category1TicketCategoryId,
            @Value("${app.demo.category2-ticket-category-id}") UUID category2TicketCategoryId
    ) {
        return args -> {
            if (inventoryRepository.findByTicketCategoryId(category1TicketCategoryId).isEmpty()) {
                inventoryRepository.save(TicketInventory.create(category1TicketCategoryId, 100));
            }
            if (inventoryRepository.findByTicketCategoryId(category2TicketCategoryId).isEmpty()) {
                inventoryRepository.save(TicketInventory.create(category2TicketCategoryId, 500));
            }
        };
    }
}
