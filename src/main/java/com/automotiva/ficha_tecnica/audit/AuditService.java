package com.automotiva.ficha_tecnica.audit;

import com.automotiva.ficha_tecnica.security.SecurityHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void register(String action, String target, String details) {
        AuditEvent event = new AuditEvent();
        event.setAction(trim(action, 60));
        event.setActor(resolveCurrentActor());
        event.setTarget(trim(target, 180));
        event.setDetails(trim(details, 500));
        event.setCreatedAt(LocalDateTime.now());

        repository.save(event);

        log.info("audit action={} actor={} target={}", event.getAction(), event.getActor(), event.getTarget());
    }

    private String resolveCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anon";
        }

        return SecurityHashUtil.pseudonymize(authentication.getName());
    }

    private String trim(String value, int max) {
        if (value == null) {
            return "";
        }
        String sanitized = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (sanitized.length() <= max) {
            return sanitized;
        }
        return sanitized.substring(0, max);
    }
}
