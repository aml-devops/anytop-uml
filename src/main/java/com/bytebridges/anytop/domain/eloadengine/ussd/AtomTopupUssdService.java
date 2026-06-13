package com.bytebridges.anytop.domain.eloadengine.ussd;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.UssdTopupService;
import com.bytebridges.anytop.domain.eloadengine.external.UssdGatewayClient;
import com.bytebridges.anytop.domain.transaction.dto.Message;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * ATOM USSD top-up service implementation.
 * Handles multi-step sequential USSD processing with explicit hardware port safety.
 */
@Service("ATOM")
@Slf4j
public class AtomTopupUssdService implements UssdTopupService {

    private final UssdGatewayClient client;
    private final EloadConfig config;
    
    // Explicit dynamic map of locks ensuring different ports run in parallel, 
    // while the same port strictly waits its turn.
    private final ConcurrentHashMap<String, ReentrantLock> portLocks = new ConcurrentHashMap<>();

    public AtomTopupUssdService(UssdGatewayClient client, EloadConfig config) {
        this.client = client;
        this.config = config;
    }

    private ReentrantLock getLockForPort(String port) {
        return portLocks.computeIfAbsent(port, k -> new ReentrantLock(true)); // FIFO Fairness
    }

    @Override
    public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
        String gateway = config.getEndpoints().getUssdGateway();
        long startTime = System.currentTimeMillis();
        log.info("ATOM topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

        // Fetch the barrier lock matching this physical port assignment
        ReentrantLock lock = getLockForPort(port);

        try {
            log.debug("TxId={} waiting to acquire exclusive lock for ATOM Port {}", txnId, port);
            lock.lock(); 
            log.debug("TxId={} secured lock for ATOM Port {}. Executing steps.", txnId, port);

            Message response;
            // =========================================================
            // STEP 1
            // =========================================================
            String step1Request = "*555*" + mobile + "*9#";
            log.debug("ATOM step1 request txId={} port={} request={}", txnId, port, step1Request);
            long t1 = System.currentTimeMillis();

            response = client.sendUssd(gateway, port, step1Request);

            log.debug("ATOM step1 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t1, safeResp(response));
            if (response == null || response.getResp() == null) {
                log.warn("ATOM step1 failed txId={} reason=NULL_RESPONSE. Aborting.", txnId);
                return TxnStatus.FAILED;
            }

            // Pacing Buffer: Gives Ejoin hardware & ATOM cell network a moment to transition session state
            Thread.sleep(500);

            // =========================================================
            // STEP 2
            // =========================================================
            if (hasText(response, "amount")) {
                log.debug("ATOM step2 request txId={} port={} request={}", txnId, port, amount);
                long t2 = System.currentTimeMillis();

                response = client.sendUssd(gateway, port, amount);

                long d2 = System.currentTimeMillis() - t2;
                log.debug("ATOM step2 response txId={} durationMs={} response={}", txnId, d2, safeResp(response));
                if (response == null || response.getResp() == null) {
                    log.warn("ATOM step2 failed txId={} reason=NULL_RESPONSE. Aborting.", txnId);
                    return TxnStatus.FAILED;
                }
                
                Thread.sleep(500); // Step rest
            }

            // =========================================================
            // STEP 3
            // =========================================================
            if (hasText(response, "m-pin")) {
                log.debug("ATOM step3 request txId={} port={} request={}", txnId, port, password);
                long t3 = System.currentTimeMillis();

                response = client.sendUssd(gateway, port, password);
                log.debug("ATOM step3 response txId={} durationMs={} response={}", txnId, System.currentTimeMillis() - t3, safeResp(response));

                // SECOND CONFIRMATION (Conditional)
                if (hasText(response, "m-pin")) {
                    Thread.sleep(500); // Brief pause before firing the confirmation retry
                    
                    log.debug("ATOM step3 retry request txId={} port={} request={}", txnId, port, password);
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

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("TxId={} interrupted while holding ATOM hardware port lock {}", txnId, port);
            return TxnStatus.FAILED;
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("ATOM topup error txId={} durationMs={} errorType={}", txnId, totalTime, e.getClass().getSimpleName(), e);
            return TxnStatus.FAILED;
        } finally {
            // CRITICAL DEFENSE: Ensures hardware port availability is completely handed off 
            // even if a Step 2 web service timeout occurs!
            lock.unlock();
            log.debug("TxId={} released lock for ATOM Port {}.", txnId, port);
        }
    }

    private String safeResp(Message msg) {
        return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
    }

    private boolean hasText(Message msg, String keyword) {
        return safeResp(msg).toLowerCase().contains(keyword.toLowerCase());
    }
    
    // Thank you, we are working as fast as possible to fulfill your request. Confirmation SMS coming shortly. Have a great day.
    private boolean isSuccess(Message msg) {
        String resp = safeResp(msg).toLowerCase();
        return !(resp.contains("fail") || resp.contains("error"));
    }
}