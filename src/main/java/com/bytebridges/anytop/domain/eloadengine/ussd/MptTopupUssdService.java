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
 * MPT USSD top-up service implementation.
 *
 * Handles multi-step USSD airtime top-up transactions through the external USSD
 * Gateway integration.
 */
@Service("MPT")
@RequiredArgsConstructor
@Slf4j
public class MptTopupUssdService implements UssdTopupService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
		String gateway = config.getEndpoints().getUssdGateway();
		long startTime = System.currentTimeMillis();
		log.info("MPT topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

		try {
			Message response;
			// =========================================================
			// STEP 1
			// =========================================================
			String step1Request = "*125*" + amount + "*" + mobile + "*" + password + "#";
			log.debug("MPT step1 request txId={} request={}", txnId, step1Request);
			long t1 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step1Request);
			
			log.debug("MPT step1 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t1, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MPT step1 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 2
			// =========================================================
			String step2Request = "1";
			log.debug("MPT step2 request txId={} request={}", txnId, step2Request);
			long t2 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step2Request);
			
			log.debug("MPT step2 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t2, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MPT step2 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 3
			// =========================================================
			String step3Request = "1";
			if (needSecondConfirm(response)) {
				log.debug("MPT step3 request txId={} request={}", txnId, step3Request);
				long t3 = System.currentTimeMillis();

				response = client.sendUssd(gateway, port, step3Request);
				
				log.debug("MPT step3 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t3, safeResp(response));
			}

			long totalTime = System.currentTimeMillis() - startTime;
			if (isSuccess(response)) {
				log.info("MPT topup success txId={} durationMs={}", txnId, totalTime);
				return TxnStatus.SUCCESS;
			}
			log.warn("MPT topup failed txId={} durationMs={}", txnId, totalTime);
			return TxnStatus.FAILED;
		} catch (Exception e) {
			long totalTime = System.currentTimeMillis() - startTime;
			log.error("MPT topup error txId={} durationMs={} errorType={}", txnId, totalTime, e.getClass().getSimpleName(), e);
			return TxnStatus.FAILED;
		}
	}

	// ✅ Safe response logging (avoid NPE)
	private String safeResp(Message msg) {
		return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
	}

	private boolean needSecondConfirm(Message msg) {
		return safeResp(msg).toLowerCase().contains("confirm");
	}

	private boolean isSuccess(Message msg) {
		return safeResp(msg).toLowerCase().contains("success");
	}

}