package com.bytebridges.anytop.domain.eloadengine.exception;

/**
 * Exception thrown when USSD Gateway communication fails.
 */
public class UssdGatewayException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UssdGatewayException(String message) {
		super(message);
	}

	public UssdGatewayException(String message, Throwable cause) {
		super(message, cause);
	}
}
