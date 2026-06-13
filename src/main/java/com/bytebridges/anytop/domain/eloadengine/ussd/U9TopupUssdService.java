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
 * U9 USSD top-up service implementation.
 *
 * Handles USSD airtime top-up transactions through the external USSD Gateway
 * integration with explicit port hardware isolation.
 */
@Service("U9")
@Slf4j
public class U9TopupUssdService implements UssdTopupService {

    private final UssdGatewayClient client;
    private final EloadConfig config;
    
    // Shared thread-safe lock pool across the U9 service instances
    private final ConcurrentHashMap<String, ReentrantLock> portLocks = new ConcurrentHashMap<>();

    public U9TopupUssdService(UssdGatewayClient client, EloadConfig config) {
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
        log.info("U9 topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

        // Fetch the barrier lock matching this physical port assignment
        ReentrantLock lock = getLockForPort(port);

        try {
            log.debug("TxId={} waiting to acquire exclusive lock for U9 Port {}", txnId, port);
            lock.lock(); 
            log.debug("TxId={} secured lock for U9 Port {}. Executing USSD request.", txnId, port);

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
                log.warn("U9 step1 failed txId={} reason=NULL_RESPONSE", txnId);
                return TxnStatus.FAILED;
            }

            // Optional structural pause: give the hardware 300-500ms to settle down 
            // before releasing the lock to the next transaction.
            Thread.sleep(500);

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

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("TxId={} interrupted while holding U9 port lock {}", txnId, port);
            return TxnStatus.FAILED;
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("U9 topup error txId={} durationMs={} errorType={}", txnId, totalTime, e.getClass().getSimpleName(), e);
            return TxnStatus.FAILED;
        } finally {
            // ALWAYS unlock the hardware port in the finally block!
            lock.unlock();
            log.debug("TxId={} released lock for U9 Port {}.", txnId, port);
        }
    }

    private String safeResp(Message msg) {
        return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
    }
    
    // Success. Thank you your transaction is executed successfully. Ref. 796786936
    // Sorry, Your transaction is failed
    // Incorrect input.
    private boolean isSuccess(Message msg) {
        return safeResp(msg).toLowerCase().contains("success");
    }
}