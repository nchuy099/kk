package com.eventhub.apigateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gateway")
public record GatewayProperties(
        Routes routes,
        RateLimit rateLimit
) {
    public record Routes(
            String eventsBaseUrl,
            String ordersBaseUrl,
            String paymentsBaseUrl,
            String usersBaseUrl,
            String ticketsBaseUrl
    ) {
    }

    public record RateLimit(
            int requestsPerMinute
    ) {
    }
}
