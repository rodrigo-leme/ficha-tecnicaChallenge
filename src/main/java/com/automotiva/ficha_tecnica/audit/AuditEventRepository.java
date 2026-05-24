package com.automotiva.ficha_tecnica.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    @Modifying
    @Query("delete from AuditEvent e where e.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
