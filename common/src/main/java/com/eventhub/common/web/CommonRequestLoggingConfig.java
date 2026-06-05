package com.eventhub.common.web;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class CommonRequestLoggingConfig {
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    OncePerRequestFilter requestLoggingFilter(@Value("${spring.application.name}") String serviceName) {
        return new OncePerRequestFilter() {
            private final Logger log = LoggerFactory.getLogger(CommonRequestLoggingConfig.class);

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                var start = Instant.now();
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    var elapsed = Duration.between(start, Instant.now()).toMillis();
                    var traceId = MDC.get("traceId");
                    var principal = request.getUserPrincipal();
                    log.info("{} {} {} -> {} {}ms traceId={} principal={}",
                            serviceName,
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus(),
                            elapsed,
                            traceId == null ? "-" : traceId,
                            principal == null ? "-" : principal.getName());
                }
            }
        };
    }
}
