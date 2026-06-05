package com.eventhub.apigateway;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatewayProxyService {
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailers",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length",
            "authorization",
            "x-user-id",
            "x-username",
            "x-user-email",
            "x-user-roles",
            "x-b3-traceid",
            "x-b3-spanid",
            "x-b3-parentspanid",
            "x-b3-sampled",
            "traceparent",
            "tracestate",
            "baggage"
    );

    private final GatewayRouteResolver routeResolver;
    private final HttpClient httpClient;
    private final Tracer tracer;

    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        var target = routeResolver.resolve(request);
        if (target == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"No route matched\"}");
            return;
        }

        var body = request.getInputStream().readAllBytes();
        var method = HttpMethod.valueOf(request.getMethod());
        var outbound = HttpRequest.newBuilder(target)
                .timeout(Duration.ofSeconds(10))
                .method(method.name(), requiresBody(method) ? HttpRequest.BodyPublishers.ofByteArray(body) : HttpRequest.BodyPublishers.noBody());
        copyRequestHeaders(request, outbound);
        addAuthenticatedUserHeaders(outbound);
        addTraceHeaders(outbound);

        var downstream = httpClient.send(outbound.build(), HttpResponse.BodyHandlers.ofByteArray());
        copyResponse(response, downstream);
    }

    private static boolean requiresBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH || method == HttpMethod.DELETE;
    }

    private static void copyRequestHeaders(HttpServletRequest request, HttpRequest.Builder outbound) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            var headerName = headerNames.nextElement();
            if (HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(headerName);
            while (values.hasMoreElements()) {
                outbound.header(headerName, values.nextElement());
            }
        }
    }

    private static void addAuthenticatedUserHeaders(HttpRequest.Builder outbound) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return;
        }
        Jwt jwt = jwtAuthentication.getToken();
        outbound.header("X-User-Id", jwt.getSubject());
        addOptionalHeader(outbound, "X-Username", jwt.getClaimAsString("preferred_username"));
        addOptionalHeader(outbound, "X-User-Email", jwt.getClaimAsString("email"));
        outbound.header("X-User-Roles", roles(jwtAuthentication.getAuthorities()));
    }

    private static void addOptionalHeader(HttpRequest.Builder outbound, String name, String value) {
        if (value != null && !value.isBlank()) {
            outbound.header(name, value);
        }
    }

    private static String roles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private void addTraceHeaders(HttpRequest.Builder outbound) {
        var span = tracer.currentSpan();
        if (span == null || span.context() == null) {
            return;
        }
        var context = span.context();
        var traceId = context.traceId();
        var parentSpanId = context.spanId();
        var childSpanId = randomSpanId();
        outbound.header("X-B3-TraceId", traceId);
        outbound.header("X-B3-SpanId", childSpanId);
        outbound.header("X-B3-ParentSpanId", parentSpanId);
        outbound.header("X-B3-Sampled", "1");
        outbound.header("traceparent", "00-" + traceId + "-" + childSpanId + "-01");
    }

    private static void copyResponse(HttpServletResponse response, HttpResponse<byte[]> downstream) throws IOException {
        response.setStatus(downstream.statusCode());
        HttpHeaders headers = downstream.headers();
        for (var entry : headers.map().entrySet()) {
            if (HOP_BY_HOP_HEADERS.contains(entry.getKey().toLowerCase())) {
                continue;
            }
            for (var value : entry.getValue()) {
                response.addHeader(entry.getKey(), value);
            }
        }
        var body = downstream.body();
        if (body != null && body.length > 0) {
            response.getOutputStream().write(body);
        }
    }

    private static String randomSpanId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
