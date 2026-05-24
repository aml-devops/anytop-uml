package com.bytebridges.anytop.auth.entity;

import java.time.LocalDateTime;

import com.bytebridges.anytop.auth.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean active = true;

    private Boolean mfaEnabled = false;

    private String mfaSecret;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}