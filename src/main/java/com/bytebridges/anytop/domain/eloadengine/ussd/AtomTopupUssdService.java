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
 * ATOM USSD top-up service implementation.
 *
 * Handles multi-step USSD airtime top-up transactions through the external USSD
 * Gateway integration.
 */
@Service("ATOM")
@RequiredArgsConstructor
@Slf4j
public class AtomTopupUssdService implements UssdTopupService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	@Override
	public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
		String gateway = config.getEndpoints().getUssdGateway();
		long startTime = System.currentTimeMillis();
		log.info("ATOM topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

		try {
			Message response;
			// =========================================================
			// STEP 1
			// =========================================================
			String step1Request = "*555*" + mobile + "*9#";
			log.debug("ATOM step1 request txId={} request={}", txnId, step1Request);
			long t1 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step1Request);

			log.debug("ATOM step1 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t1, safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("ATOM step1 failed txId={} reason=NULL_RESPONSE", txnId);
				return TxnStatus.FAILED;
			}

			// =========================================================
			// STEP 2
			// =========================================================
			if (hasText(response, "amount")) {
				log.debug("ATOM step2 request txId={} request={}", txnId, amount);
				long t2 = System.currentTimeMillis();

				response = client.sendUssd(gateway, port, amount);

				long d2 = System.currentTimeMillis() - t2;
				log.debug("ATOM step2 response txId={} durationMs={} response={}", txnId, d2, safeResp(response));
				if (response == null || response.getResp() == null) {
					log.warn("ATOM step2 failed txId={} reason=NULL_RESPONSE", txnId);
					return TxnStatus.FAILED;
				}
			}

			// =========================================================
			// STEP 3
			// =========================================================
			if (hasText(response, "m-pin")) {
				log.debug("ATOM step3 request txId={} request={}", txnId, password);
				long t3 = System.currentTimeMillis();

				response = client.sendUssd(gateway, port, password);

				log.debug("ATOM step3 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t3, safeResp(response));

				// SECOND CONFIRMATION
				if (hasText(response, "m-pin")) {
					log.debug("ATOM step3 retry request txId={} request={}", txnId, password);
					long t4 = System.currentTimeMillis();

					response = client.sendUssd(gateway, port, password);

					long d4 = System.currentTimeMillis() - t4;
					log.debug("ATOM step3 retry response txId={} durationMs={} response={}", txnId, d4, safeResp(response));
				}
			}

			// =========================================================
			// FINAL RESULT
			// =========================================================
			long totalTime = System.currentTimeMillis() - startTime;
			if (isSuccess(response)) {
				log.info("ATOM topup success txId={} durationMs={}", txnId, totalTime);
				return TxnStatus.SUCCESS;
			}
			log.warn("ATOM topup failed txId={} durationMs={}", txnId, totalTime);
			return TxnStatus.FAILED;
		} catch (Exception e) {
			long totalTime = System.currentTimeMillis() - startTime;
			log.error("ATOM topup error txId={} durationMs={} errorType={}", txnId, totalTime, e.getClass().getSimpleName(), e);
			return TxnStatus.FAILED;
		}
	}

	// ✅ Safe response
	private String safeResp(Message msg) {
		return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
	}

	// ✅ Case-insensitive match
	private boolean hasText(Message msg, String keyword) {
		return safeResp(msg).toLowerCase().contains(keyword.toLowerCase());
	}

	private boolean isSuccess(Message msg) {
		String resp = safeResp(msg).toLowerCase();
		return !(resp.contains("fail") || resp.contains("error"));
	}
}
