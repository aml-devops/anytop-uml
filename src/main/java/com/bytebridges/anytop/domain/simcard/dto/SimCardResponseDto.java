package com.bytebridges.anytop.domain.simcard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SimCardResponseDto {

    private Long id;
    private String msisdn;
    private String operator;
    private String simName;
    private Boolean isActive;
    private Integer balance;
}