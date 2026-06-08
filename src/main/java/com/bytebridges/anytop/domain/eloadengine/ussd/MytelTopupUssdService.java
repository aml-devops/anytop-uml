package com.bytebridges.anytop.domain.eloadengine.ussd;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.UssdTopupService;
import com.bytebridges.anytop.domain.eloadengine.enums.MytelAmount;
import com.bytebridges.anytop.domain.eloadengine.external.UssdGatewayClient;
import com.bytebridges.anytop.domain.transaction.dto.Message;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MYTEL USSD top-up service implementation.
 *
 * Handles USSD airtime top-up transactions through the external USSD Gateway
 * integration.
 */
@Service("MYTEL")
@RequiredArgsConstructor
@Slf4j
public class MytelTopupUssdService implements UssdTopupService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	@Override
	public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
		String gateway = config.getEndpoints().getUssdGateway();
		long startTime = System.currentTimeMillis();		
		log.info("MYTEL topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

		try {
			Message response;
			// =========================================================
			// STEP 1
			// =========================================================
			String step1Request = "*888#";
			log.debug("MYTEL step1 request txId={} port={} request={}", txnId, port, step1Request);
			long t1 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step1Request);

			log.debug("MYTEL step1 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t1, safeResp(response));
			
			if (response == null || response.getResp() == null) {
				log.warn("MYTEL step1 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 2
			// =========================================================			
			String step2Request = "1";
			log.debug("MYTEL step2 request txId={} port={} request={}", txnId, port, step2Request);
			long t2 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step2Request);

			log.debug("MYTEL step2 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t2, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MYTEL step2 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 3
			// =========================================================
			String step3Request = "1";
			log.debug("MYTEL step3 request txId={} port={} request={}", txnId, port, step3Request);
			long t3 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step3Request);			

			log.debug("MYTEL step3 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t3, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MYTEL step3 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 4
			// =========================================================
			log.debug("MYTEL step4 request txId={} port={} request={}", txnId, port, mobile);
			long t4 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, mobile);

			log.debug("MYTEL step4 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t4, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MYTEL step4 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 5
			// =========================================================
			String amountCode = MytelAmount.fromAmount(amount);
			log.debug("MYTEL step5 request txId={} port={} request={}", txnId, port, amountCode);
			long t5 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, amountCode);

			log.debug("MYTEL step5 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t5, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MYTEL step5 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 6
			// =========================================================
			log.debug("MYTEL step6 request txId={} port={} request={}", txnId, port, password);
			long t6 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, password);

			log.debug("MYTEL step6 response txId={} port={} mobile={} durationMs={} response={}", txnId, System.currentTimeMillis() - t6, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MYTEL step6 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 7
			// =========================================================
			String step7Request = "1";
			log.debug("MYTEL step7 request txId={} port={} request={}", txnId, port, step7Request);
			long t7 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step7Request);

			log.debug("MYTEL step7 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t7, safeResp(response));

			// =========================================================
			// FINAL RESULT
			// =========================================================
			long totalTime = System.currentTimeMillis() - startTime;
			if (isSuccess(response)) {
				log.info("MYTEL topup success txId={} totalDurationMs={}", txnId, totalTime);
				return TxnStatus.SUCCESS;
			}
			log.warn("MYTEL topup failed txId={} totalDurationMs={}", txnId, totalTime);
			return TxnStatus.FAILED;
		} catch (Exception e) {
			long totalTime = System.currentTimeMillis() - startTime;
			log.error("MYTEL topup error txId={} durationMs={} errorType={}", txnId, totalTime, e.getClass().getSimpleName(), e);
			return TxnStatus.FAILED;
		}
	}

	private String safeResp(Message msg) {
		return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
	}
	
	// Transaction is successful. Detail information in your mesage. Thanks
	// Transaction is unsuccessful for the subscriber 9521. Please try again later! Transaction id is 1524185631
	private boolean isSuccess(Message msg) {
	    String resp = safeResp(msg).toLowerCase();

	    return resp.contains("successful")
	            && !resp.contains("unsuccessful");
	}
}