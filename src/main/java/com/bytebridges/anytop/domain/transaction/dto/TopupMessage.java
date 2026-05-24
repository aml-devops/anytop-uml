package com.bytebridges.anytop.domain.transaction.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopupMessage {

    private Long txnId;
    private String operator;
    private String messageId;
}
