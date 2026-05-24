package com.bytebridges.anytop.domain.transaction.enums;

public enum TxnStatus {
	// Request accepted and queued
	QUEUED,
	
	UPLOADED,

	// Consumer picked transaction
	PROCESSING,

	// GSM/USSD completed successfully
	SUCCESS,

	// GSM/USSD failed
	FAILED,
	
	// exception / crash / timeout
    FAILED_SYSTEM      
}
