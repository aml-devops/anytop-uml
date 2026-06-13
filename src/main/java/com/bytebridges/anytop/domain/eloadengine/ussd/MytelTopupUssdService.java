package com.bytebridges.anytop.domain.eloadengine.ussd;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.bytebridges.anytop.config.EloadConfig;
import com.bytebridges.anytop.domain.eloadengine.UssdTopupService;
import com.bytebridges.anytop.domain.eloadengine.enums.MytelAmount;
import com.bytebridges.anytop.domain.eloadengine.external.UssdGatewayClient;
import com.bytebridges.anytop.domain.transaction.dto.Message;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;

import lombok.extern.slf4j.Slf4j;

@Service("MYTEL")
@Slf4j
public class MytelTopupUssdService implements UssdTopupService {

    private final UssdGatewayClient client;
    private final EloadConfig config;
    
    // Lock pool for hardware port isolation
    private final ConcurrentHashMap<String, ReentrantLock> portLocks = new ConcurrentHashMap<>();

    public MytelTopupUssdService(UssdGatewayClient client, EloadConfig config) {
        this.client = client;
        this.config = config;
    }

    private ReentrantLock getLockForPort(String port) {
        return portLocks.computeIfAbsent(port, k -> new ReentrantLock(true));
    }

    @Override
    public TxnStatus topup(Long txnId, String port, String password, String mobile, String amount) {
        String gateway = config.getEndpoints().getUssdGateway();
        long startTime = System.currentTimeMillis();
        
        ReentrantLock lock = getLockForPort(port);

        try {
            lock.lock(); // Ensure exclusive access to this physical SIM for all 7 steps
            log.info("MYTEL topup started txId={} port={} mobile={} amount={}", txnId, port, mobile, amount);

            Message response = new Message();
            // Loop through steps 1-7
            // We use a small buffer sleep to ensure the hardware finishes processing before the next USSD command
            for (int step = 1; step <= 7; step++) {
                String currentRequest = getRequestForStep(step, mobile, amount, password);
                log.debug("MYTEL step{} request txId={} request={}", step, txnId, currentRequest);
                
                long t = System.currentTimeMillis();
                response = client.sendUssd(gateway, port, currentRequest);
                log.debug("MYTEL step{} response txId={} durationMs={} response={}", step, txnId, System.currentTimeMillis() - t, safeResp(response));

                if (response == null || response.getResp() == null) {
                    log.warn("MYTEL step{} failed txId={} reason=NULL_RESPONSE", step, txnId);
                    return TxnStatus.FAILED;
                }
                
                Thread.sleep(500); // Critical delay for 7-step sequence stability
            }

            long totalTime = System.currentTimeMillis() - startTime;
            if (isSuccess(response)) {
                log.info("MYTEL topup success txId={} totalDurationMs={}", txnId, totalTime);
                return TxnStatus.SUCCESS;
            }
            log.warn("MYTEL topup failed txId={} totalDurationMs={}", txnId, totalTime);
            return TxnStatus.FAILED;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TxnStatus.FAILED;
        } catch (Exception e) {
            log.error("MYTEL topup error txId={} errorType={}", txnId, e.getClass().getSimpleName(), e);
            return TxnStatus.FAILED;
        } finally {
            lock.unlock();
        }
    }

    private String getRequestForStep(int step, String mobile, String amount, String password) {
        return switch (step) {
            case 1 -> "*888#";
            case 2, 3, 7 -> "1";
            case 4 -> mobile;
            case 5 -> MytelAmount.fromAmount(amount);
            case 6 -> password;
            default -> "";
        };
    }

    private String safeResp(Message msg) {
        return (msg != null && msg.getResp() != null) ? msg.getResp() : "NULL";
    }
    
    // Transaction is successful. Detail information in your mesage. Thanks
	// Transaction is unsuccessful for the subscriber 9521. Please try again later! Transaction id is 1524185631
    private boolean isSuccess(Message msg) {
        String resp = safeResp(msg).toLowerCase();
        return resp.contains("successful") && !resp.contains("unsuccessful");
    }
}