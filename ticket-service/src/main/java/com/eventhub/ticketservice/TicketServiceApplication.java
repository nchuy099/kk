package com.eventhub.ticketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.eventhub.common.web.CommonRequestLoggingConfig;

@SpringBootApplication
@EnableScheduling
@Import(CommonRequestLoggingConfig.class)
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}
