package com.eventhub.apigateway;

import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatewayRouteResolver {
    private final GatewayProperties properties;

    public URI resolve(HttpServletRequest request) {
        var path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return null;
        }
        var baseUrl = baseUrlFor(path);
        if (baseUrl == null) {
            return null;
        }
        var downstreamPath = path.substring("/api".length());
        var query = request.getQueryString();
        var uri = baseUrl + downstreamPath;
        if (query != null && !query.isBlank()) {
            uri += "?" + query;
        }
        return URI.create(uri);
    }

    private String baseUrlFor(String path) {
        if (path.matches("^/api/orders/[^/]+/tickets$")) {
            return properties.routes().ticketsBaseUrl();
        }
        if (path.startsWith("/api/competitions")
                || path.startsWith("/api/events")
                || path.startsWith("/api/stadiums")
                || path.startsWith("/api/ticket-categories")
                || path.startsWith("/api/ticket-types")
                || path.startsWith("/api/admin/events")
                || path.startsWith("/api/admin/competitions")
                || path.startsWith("/api/admin/stadiums")) {
            return properties.routes().eventsBaseUrl();
        }
        if (path.startsWith("/api/orders")) {
            return properties.routes().ordersBaseUrl();
        }
        if (path.startsWith("/api/payments")) {
            return properties.routes().paymentsBaseUrl();
        }
        if (path.startsWith("/api/users") || path.startsWith("/api/admin/users")) {
            return properties.routes().usersBaseUrl();
        }
        if (path.startsWith("/api/tickets")) {
            return properties.routes().ticketsBaseUrl();
        }
        return null;
    }
}
