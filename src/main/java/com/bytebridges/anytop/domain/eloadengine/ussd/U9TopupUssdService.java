package com.bytebridges.anytop.domain.eloadengine.ussd;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.UssdTopupService;
import com.bytebridges.anytop.domain.eloadengine.external.UssdGatewayClient;
import com.bytebridges.anytop.domain.transaction.dto.Message;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * U9 USSD top-up service implementation.
 *
 * Handles USSD airtime top-up transactions through the external USSD Gateway
 * integration.
 */
@Service("U9")
@RequiredArgsConstructor
@Slf4j
public class U9TopupUssdService implements UssdTopupService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	@Override
	public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
		String gateway = config.getEndpoints().getUssdGateway();
		long startTime = System.currentTimeMillis();
		log.info("U9 topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

		try {
			Message response;
			// =========================================================
			// STEP 1
			// =========================================================
			String step1Request = "*116*1*" + mobile + "*" + amount + "*" + password + "*1#";
			log.debug("U9 step1 request txId={} port={} request={}", txnId, port, step1Request);
			long t1 = System.currentTimeMillis();
			
			response = client.sendUssd(gateway, port, step1Request);
			
			long d1 = System.currentTimeMillis() - t1;
			log.debug("U9 step1 response txId={} durationMs={} response={}", txnId, d1, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("U9 step1 failed txId={}  reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// FINAL RESULT
			// =========================================================
			long totalTime = System.currentTimeMillis() - startTime;
			if (isSuccess(response)) {
				log.info("U9 topup success txId={} totalDurationMs={}", txnId, totalTime);
				return TxnStatus.SUCCESS;
			}
			log.warn("U9 topup failed txId={} totalDurationMs={}", txnId, totalTime);
			return TxnStatus.FAILED;

		} catch (Exception e) {
			long totalTime = System.currentTimeMillis() - startTime;
			log.error("U9 topup error txId={} durationMs={} errorType={}", txnId, totalTime, e.getClass().getSimpleName(), e);
			return TxnStatus.FAILED;
		}
	}

	private String safeResp(Message msg) {
		return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
	}
	
	/**
	private boolean isSuccess(Message msg) {
		String resp = safeResp(msg).toLowerCase();
		return !(resp.contains("fail") || resp.contains("error"));
	}*/
	
	// Success. Thank you your transaction is executed successfully. Ref. 796786936
	private boolean isSuccess(Message msg) {
		return safeResp(msg).toLowerCase().contains("success");
	}
}