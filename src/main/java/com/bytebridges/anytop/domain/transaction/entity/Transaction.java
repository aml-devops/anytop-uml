package com.bytebridges.anytop.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;

    private Integer amount;

    private String operator;
    
    @Enumerated(EnumType.STRING)
    private TxnStatus status;

    private Long simId;
    
    private String messageId;

    private LocalDateTime createdAt;
}
