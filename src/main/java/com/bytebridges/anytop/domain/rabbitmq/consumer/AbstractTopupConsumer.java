package com.bytebridges.anytop.domain.rabbitmq.consumer;

import com.bytebridges.anytop.domain.rabbitmq.service.GsmWorker;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTopupConsumer {

    protected final TransactionRepository transactionRepository;
    protected final GsmWorker gsmWorker;

    protected void handle(String operator, TopupMessage message) {
        long start = System.currentTimeMillis();
        Long txnId = null;

        try {
            log.info("{} message received txnId={} messageId={}", operator, message.getTxnId(), message.getMessageId());

            // 1. Find the transaction
            Transaction txn = transactionRepository.findByMessageId(message.getMessageId()).orElseThrow(
                    () -> new IllegalStateException("Transaction not found messageId=" + message.getMessageId()));

            txnId = txn.getId();

            // 2. Idempotency Check: Skip if already done or currently running elsewhere
            if (txn.getStatus() == TxnStatus.PROCESSING || txn.getStatus() == TxnStatus.SUCCESS) {
                log.warn("{} duplicate skipped txnId={} status={}", operator, txnId, txn.getStatus());
                return;
            }

            // 3. Optimistic Lock/Atomic Update: Mark as PROCESSING in the DB
            int updated = transactionRepository.markProcessing(txnId);
            if (updated == 0) {
                log.warn("{} processing skipped txnId={} reason=ALREADY_PROCESSING", operator, txnId);
                return;
            }

            // 4. Execute Synchronously on this RabbitMQ worker thread
            // This blocks the individual RabbitMQ consumer channel for 6-10 seconds, which is correct!
            gsmWorker.execute(txnId);

            log.info("{} worker successfully completed txnId={} durationMs={}", operator, txnId, System.currentTimeMillis() - start);

        } catch (IllegalStateException ex) {
            // Business logic failure (e.g., Row not found / Data bad). 
            // We catch it and log it, letting the method finish so RabbitMQ ACKs and drops the bad message.
            log.error("{} business logic failure txnId={} error={}", operator, message.getTxnId(), ex.getMessage());
            
        } catch (Exception ex) {
            // Infrastructure or hardware failure (e.g., DB down, network timeout, GSM pool full).
            log.error("{} critical consumer exception thrown txnId={}. Re-queueing message.", operator, message.getTxnId(), ex);
            
            // Revert state back to FAILED or PENDING in DB so the system can try again later
            if (txnId != null) {
                try {
                    transactionRepository.updateStatus(txnId, TxnStatus.FAILED.name());
                    log.info("Successfully reset database status to FAILED for txnId={}", txnId);
                } catch (Exception dbEx) {
                    log.error("Failed to reset database status for txnId={}", txnId, dbEx);
                }
            }
            
            // CRITICAL: Rethrow the exception! This tells Spring AMQP to reject the message,
            // putting it back into the RabbitMQ Queue for a retry instead of dropping it.
            throw ex; 
        }
    }
}