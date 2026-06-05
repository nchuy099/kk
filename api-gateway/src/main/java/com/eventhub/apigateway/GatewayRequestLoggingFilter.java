package com.eventhub.apigateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class GatewayRequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(GatewayRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var start = Instant.now();
        try {
            filterChain.doFilter(request, response);
        } finally {
            var elapsed = Duration.between(start, Instant.now()).toMillis();
            var traceId = MDC.get("traceId");
            var principal = request.getUserPrincipal();
            var principalName = principal == null ? "-" : principal.getName();
            log.info("gateway {} {} -> {} {}ms traceId={} principal={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsed,
                    traceId == null ? "-" : traceId,
                    principalName);
        }
    }
}
