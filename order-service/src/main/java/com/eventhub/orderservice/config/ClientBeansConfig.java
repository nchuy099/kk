package com.eventhub.orderservice.config;

import com.eventhub.orderservice.client.EventServiceClient;
import com.eventhub.orderservice.client.InventoryServiceClient;
import com.eventhub.orderservice.client.PaymentServiceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeansConfig {

    @Bean
    EventServiceClient eventServiceClient(@Qualifier("eventServiceRestClient") RestClient restClient) {
        return new EventServiceClient(restClient);
    }

    @Bean
    InventoryServiceClient inventoryServiceClient(@Qualifier("inventoryServiceRestClient") RestClient restClient) {
        return new InventoryServiceClient(restClient);
    }

    @Bean
    PaymentServiceClient paymentServiceClient(@Qualifier("paymentServiceRestClient") RestClient restClient) {
        return new PaymentServiceClient(restClient);
    }
}

