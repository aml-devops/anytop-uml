package com.bytebridges.anytop.domain.auditlog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.bytebridges.anytop.domain.auditlog.entity.AuditLog;
import com.bytebridges.anytop.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String username,
                    String action,
                    String endpoint,
                    String ipAddress) {

        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .endpoint(endpoint)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
