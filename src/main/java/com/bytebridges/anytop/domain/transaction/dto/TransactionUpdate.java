package com.bytebridges.anytop.domain.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionUpdate {

    private Long txnId;
    private String status;
    private String simName;
    private String message;
}
