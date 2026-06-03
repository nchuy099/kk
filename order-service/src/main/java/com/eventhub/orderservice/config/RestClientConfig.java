package com.eventhub.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient eventServiceRestClient(@Value("${app.event-base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient inventoryServiceRestClient(@Value("${app.inventory-base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient paymentServiceRestClient(@Value("${app.payment-base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}

