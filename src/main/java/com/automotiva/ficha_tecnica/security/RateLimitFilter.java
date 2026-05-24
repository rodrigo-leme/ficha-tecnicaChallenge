package com.automotiva.ficha_tecnica.security;

import com.automotiva.ficha_tecnica.audit.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;

    private final SecurityProperties securityProperties;
    private final AuditService auditService;
    private final Map<String, RateWindow> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(SecurityProperties securityProperties, AuditService auditService) {
        this.securityProperties = securityProperties;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        long now = Instant.now().toEpochMilli();

        RateWindow window = windows.computeIfAbsent(clientIp, k -> new RateWindow(now));

        synchronized (window) {
            if ((now - window.windowStart) > WINDOW_MILLIS) {
                window.windowStart = now;
                window.counter.set(0);
            }

            int current = window.counter.incrementAndGet();
            if (current > securityProperties.getRateLimitRequestsPerMinute()) {
                auditService.register("SUSPICIOUS_RATE_LIMIT", "ip:" + SecurityHashUtil.pseudonymize(clientIp), "Rate limit excedido");

                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"status\":429,\"mensagem\":\"Muitas requisicoes\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private static class RateWindow {
        private long windowStart;
        private final AtomicInteger counter = new AtomicInteger(0);

        private RateWindow(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
