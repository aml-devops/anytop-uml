package com.bytebridges.anytop.domain.eloadengine.ussd;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.UssdTopupService;
import com.bytebridges.anytop.domain.eloadengine.external.UssdGatewayClient;
import com.bytebridges.anytop.domain.transaction.dto.Message;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

import lombok.extern.slf4j.Slf4j;

@Service("MPT")
@Slf4j
public class MptTopupUssdService implements UssdTopupService {

	private final UssdGatewayClient client;
	private final EloadConfig config;

	// A thread-safe pool of locks dedicated to managing each individual physical
	// SIM port
	private final ConcurrentHashMap<String, ReentrantLock> portLocks = new ConcurrentHashMap<>();

	public MptTopupUssdService(UssdGatewayClient client, EloadConfig config) {
		this.client = client;
		this.config = config;
	}

	// Helper method to dynamically manage locks for your hardware ports (16C, 16D,
	// etc.)
	private ReentrantLock getLockForPort(String port) {
		return portLocks.computeIfAbsent(port, k -> new ReentrantLock(true)); // true ensures fairness (FIFO)
	}

	public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
		String gateway = config.getEndpoints().getUssdGateway();
		long startTime = System.currentTimeMillis();
		log.info("MPT topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

		// Get the specific lock assigned to this physical port (e.g., Port 16C)
		ReentrantLock lock = getLockForPort(port);

		try {
			// Force this thread to wait until any previous transaction on this physical
			// port is 100% finished
			log.debug("TxId={} waiting to acquire exclusive hardware lock for Port {}", txnId, port);
			lock.lock();
			log.debug("TxId={} successfully locked Port {}. Starting USSD sequence.", txnId, port);

			Message response;

			// =========================================================
			// STEP 1
			// =========================================================
			String step1Request = "*125*" + amount + "*" + mobile + "*" + password + "#";
			log.debug("MPT step1 request txId={} port={} request={}", txnId, port, step1Request);
			long t1 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step1Request);

			log.debug("MPT step1 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t1,
					safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MPT step1 failed txId={} reason=NULL_RESPONSE. Aborting.", txnId);
				return TxnStatus.FAILED;
			}

			// OPTIONAL PAUSE: Give the Ejoin hardware and MPT network 500ms to clear
			// buffers before step 2
			Thread.sleep(500);

			// =========================================================
			// STEP 2
			// =========================================================
			String step2Request = "1";
			log.debug("MPT step2 request txId={} port={} request={}", txnId, port, step2Request);
			long t2 = System.currentTimeMillis();

			response = client.sendUssd(gateway, port, step2Request);

			log.debug("MPT step2 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t2,
					safeResp(response));
			if (response == null || response.getResp() == null) {
				log.warn("MPT step2 failed txId={} reason=NULL_RESPONSE. Aborting.", txnId);
				return TxnStatus.FAILED;
			}

			// OPTIONAL PAUSE: Give it another short rest before final confirmation
			Thread.sleep(500);

			// =========================================================
			// STEP 3
			// =========================================================
			String step3Request = "1";
			if (needSecondConfirm(response)) {
				log.debug("MPT step3 request txId={} port={} request={}", txnId, port, step3Request);
				long t3 = System.currentTimeMillis();

				response = client.sendUssd(gateway, port, step3Request);

				log.debug("MPT step3 response txId={} durationMs={} response={}", txnId,
						System.currentTimeMillis() - t3, safeResp(response));
			}

			// Evaluate final success state
			long totalTime = System.currentTimeMillis() - startTime;
			if (isSuccess(response)) {
				log.info("MPT topup success txId={} durationMs={}", txnId, totalTime);
				return TxnStatus.SUCCESS;
			}
			log.warn("MPT topup failed txId={} durationMs={}", txnId, totalTime);
			return TxnStatus.FAILED;

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("TxId={} interrupted while waiting/processing on Port {}", txnId, port);
			return TxnStatus.FAILED;
		} catch (Exception e) {
			long totalTime = System.currentTimeMillis() - startTime;
			log.error("MPT topup error txId={} durationMs={} errorType={}", txnId, totalTime,
					e.getClass().getSimpleName(), e);
			return TxnStatus.FAILED;
		} finally {
			// CRITICAL: Unlock the physical port so the next RabbitMQ item waiting for this
			// port can begin safely
			lock.unlock();
			log.debug("TxId={} released lock for Port {}.", txnId, port);
		}
	}

	private String safeResp(Message msg) {
		return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
	}

	private boolean needSecondConfirm(Message msg) {
		return safeResp(msg).toLowerCase().contains("confirm");
	}

	// 05-24 15:02:23 Dear agent, you have successfully transferred 1000 Ks to 959441331456.Your remaining balance is -80499 Ks.
	private boolean isSuccess(Message msg) {
		return safeResp(msg).toLowerCase().contains("success");
	}
}