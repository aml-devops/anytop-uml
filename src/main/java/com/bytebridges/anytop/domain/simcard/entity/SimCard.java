package com.bytebridges.anytop.domain.simcard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.bytebridges.anytop.domain.simcard.enums.SimStatus;

@Entity
@Table(name = "sim_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String msisdn;

    private String simName;

    private String operator; // MPT, Ooredoo, Mytel

    @Enumerated(EnumType.STRING)
    private SimStatus status; // FREE, BUSY, LOW_BALANCE, DOWN
    
    private String password;

    private Boolean isActive;

    private Integer balance;           // cached balance

    private LocalDateTime lastUsedAt;
}