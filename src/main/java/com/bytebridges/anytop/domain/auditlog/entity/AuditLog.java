package com.bytebridges.anytop.domain.auditlog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String action;

    private String endpoint;

    private String ipAddress;

    private LocalDateTime createdAt;
}
