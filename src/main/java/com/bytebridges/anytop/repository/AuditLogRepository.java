package com.bytebridges.anytop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytebridges.anytop.domain.auditlog.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
