package com.eventhub.apigateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class GatewayRateLimitFilter extends OncePerRequestFilter {
    private final GatewayProperties properties;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        var key = clientKey(request);
        if (!tryConsume(key)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean tryConsume(String key) {
        var now = clock.millis();
        var windowMillis = 60_000L;
        var state = windows.computeIfAbsent(key, ignored -> new Window(now));
        synchronized (state) {
            if (now - state.windowStart >= windowMillis) {
                state.windowStart = now;
                state.count = 0;
            }
            if (state.count >= properties.rateLimit().requestsPerMinute()) {
                return false;
            }
            state.count++;
            return true;
        }
    }

    private static String clientKey(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private long windowStart;
        private int count;

        private Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
