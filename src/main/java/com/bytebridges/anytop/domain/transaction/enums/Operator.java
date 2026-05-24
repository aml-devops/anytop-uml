package com.bytebridges.anytop.domain.transaction.enums;

public enum Operator {
	MPT, ATOM, U9, MYTEL, UNKNOWN;

	public static Operator from(String value) {
		return Operator.valueOf(value.trim().toUpperCase());
	}
}
