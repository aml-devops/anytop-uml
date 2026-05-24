package com.bytebridges.anytop.domain.simcard.projection;

public interface SimCardProjection {

	Long getId();
	
	String getMsisdn();
	
	String getOperator();

	String getSimName();

	Boolean getIsActive();

	Integer getBalance();

}
