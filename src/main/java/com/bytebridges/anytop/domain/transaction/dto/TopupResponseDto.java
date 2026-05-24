package com.bytebridges.anytop.domain.transaction.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupResponseDto {

    private Long txnId;

    private String status;

    private String message;

    private String operator;

    private String phoneNumber;

    private Integer amount;

    private String messageId;

    private LocalDateTime createdAt;
}
