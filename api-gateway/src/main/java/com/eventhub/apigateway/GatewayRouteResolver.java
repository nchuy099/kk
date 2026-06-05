package com.eventhub.apigateway;

import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class GatewayRouteResolver {
    private final GatewayProperties properties;

    public GatewayRouteResolver(GatewayProperties properties) {
        this.properties = properties;
    }

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
        if (path.startsWith("/api/events") || path.startsWith("/api/ticket-types") || path.startsWith("/api/admin/events")) {
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
