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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class PayloadSignatureFilter extends OncePerRequestFilter {

    private static final long MAX_CLOCK_SKEW_SECONDS = 300;

    private final SecurityProperties securityProperties;
    private final AuditService auditService;

    public PayloadSignatureFilter(SecurityProperties securityProperties, AuditService auditService) {
        this.securityProperties = securityProperties;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!securityProperties.isPayloadSignatureEnabled() || !shouldValidate(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String signature = request.getHeader("X-Payload-Signature");
        String timestamp = request.getHeader("X-Timestamp");

        if (signature == null || timestamp == null) {
            reject(response, request, "Assinatura obrigatoria ausente");
            return;
        }

        long now = Instant.now().getEpochSecond();
        long requestTimestamp;

        try {
            requestTimestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            reject(response, request, "Timestamp invalido");
            return;
        }

        if (Math.abs(now - requestTimestamp) > MAX_CLOCK_SKEW_SECONDS) {
            reject(response, request, "Timestamp expirado");
            return;
        }

        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
        String body = new String(wrapped.getCachedBody(), StandardCharsets.UTF_8);
        String payload = timestamp + "." + body;

        String expectedSignature;
        try {
            expectedSignature = sign(payload, securityProperties.getPayloadSignatureSecret());
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao calcular assinatura", ex);
        }

        if (!constantTimeEquals(expectedSignature, signature)) {
            reject(response, request, "Assinatura invalida");
            return;
        }

        filterChain.doFilter(wrapped, response);
    }

    private boolean shouldValidate(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        boolean mutatingMethod = "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);

        return mutatingMethod && path.startsWith("/api/veiculos");
    }

    private void reject(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        auditService.register("SUSPICIOUS_SIGNATURE", "ip:" + SecurityHashUtil.pseudonymize(request.getRemoteAddr()), message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":401,\"mensagem\":\"Requisicao sem integridade valida\"}");
    }

    private String sign(String payload, String secret) throws Exception {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("app.security.payload-signature-secret deve ter ao menos 32 caracteres");
        }

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(digest);
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        byte[] a = left.getBytes(StandardCharsets.UTF_8);
        byte[] b = right.getBytes(StandardCharsets.UTF_8);

        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
