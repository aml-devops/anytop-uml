package com.bytebridges.anytop.domain.simcard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OperatorBalanceResponseDto {

    private List<OperatorBalanceDto> operators;
    private Long grandTotalBalance;
}