package com.eventhub.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(4);

    @Bean
    RestClient eventServiceRestClient(@Value("${app.event-base-url}") String baseUrl) {
        return baseClient().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient inventoryServiceRestClient(@Value("${app.inventory-base-url}") String baseUrl) {
        return baseClient().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient paymentServiceRestClient(@Value("${app.payment-base-url}") String baseUrl) {
        return baseClient().baseUrl(baseUrl).build();
    }

    private RestClient.Builder baseClient() {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return RestClient.builder().requestFactory(requestFactory);
    }
}
