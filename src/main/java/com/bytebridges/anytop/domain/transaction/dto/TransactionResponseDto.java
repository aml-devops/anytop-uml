package com.bytebridges.anytop.domain.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private String phoneNumber;
    private Integer amount;
    private String operator;
    private String status;
    private Long simId;
    private String messageId;
    private LocalDateTime createdAt;
}
