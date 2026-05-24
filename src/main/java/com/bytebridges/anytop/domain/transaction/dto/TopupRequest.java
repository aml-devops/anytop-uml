package com.bytebridges.anytop.domain.transaction.dto;

import lombok.Data;

@Data
public class TopupRequest {
	private String operator;
	private String phone;
	private Integer amount;
}