package com.automotiva.ficha_tecnica.audit;

import com.automotiva.ficha_tecnica.security.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class AuditRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(AuditRetentionJob.class);

    private final AuditEventRepository repository;
    private final SecurityProperties securityProperties;

    public AuditRetentionJob(AuditEventRepository repository, SecurityProperties securityProperties) {
        this.repository = repository;
        this.securityProperties = securityProperties;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredData() {
        int retentionDays = Math.max(7, securityProperties.getDataRetentionDays());
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        int deleted = repository.deleteOlderThan(threshold);
        if (deleted > 0) {
            log.info("retention purge executed deletedRecords={}", deleted);
        }
    }
}
