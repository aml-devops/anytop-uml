package com.bytebridges.anytop.domain.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TransactionPageResponseDto {

    private List<TransactionResponseDto> items;
    private PaginationDto pagination;
}