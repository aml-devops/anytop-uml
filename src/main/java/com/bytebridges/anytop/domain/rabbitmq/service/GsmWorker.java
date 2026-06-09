package com.bytebridges.anytop.domain.rabbitmq.service;

import org.springframework.stereotype.Component;

import com.bytebridges.anytop.domain.eloadengine.router.UssdTopupEngine;
import com.bytebridges.anytop.domain.simcard.entity.SimCard;
import com.bytebridges.anytop.domain.simcard.service.SimPoolManager;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.enums.Operator;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GsmWorker {

    private final SimPoolManager simPoolManager;
    private final TransactionRepository transactionRepository;
    // Inject your physical GSM hardware service wrapper here
    // private final GsmModemService gsmModemService; 
    private final UssdTopupEngine ussdTopupEngine;

    public void execute(Long txnId) {
        // 1. Fetch current transaction details
        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new IllegalStateException("Transaction record missing for ID: " + txnId));
        String operator = txn.getOperator();
        // 2. Acquire SIM. Wait up to 30 seconds max if all 5 SIMs are busy.
        SimCard sim = simPoolManager.acquire(txn.getOperator()); 
        if (sim == null) {
            // If it takes > 30 seconds to get a SIM, throw an exception to trigger a consumer re-queue
            throw new RuntimeException("SIM pool exhausted. No available slots for " + txn.getOperator());
        }

        // 3. Strict try-finally to guarantee the SIM hardware resource is NEVER lost/leaked
        try {
            log.info("SIM {} [Port: {}] locked exclusively for txnId {}", sim.getSimName(), sim.getSimName(), txnId);
            
            // -------------------------------------------------------------
            // SIMULATE PHYSICAL TOP-UP PROCESS (6-10 seconds)
            // Replace this with your actual AT command / Telco gateway logic
            // -------------------------------------------------------------
            TxnStatus result = ussdTopupEngine.route(Operator.from(operator), txn.getId(), sim.getSimName(),
					sim.getPassword(), txn.getPhoneNumber(), String.valueOf(txn.getAmount()));
            
            // 4. Update transaction status to SUCCESS in the database
            // transactionRepository.updateStatus(txnId, TxnStatus.SUCCESS.name());
            // log.info("Transaction txnId={} updated to SUCCESS in database", txnId);
            transactionRepository.completeTransaction(txn.getId(), result.name(), sim.getId(), sim.getSimName());
            log.info("Topup done txnId={} operator={} simId={} result={}", txnId, operator, sim.getId(), result);

        } catch (Exception ex) {
            log.error("Hardware error processing top-up on SIM {} for txnId={}", sim.getSimName(), txnId, ex);
            // Propagate the error upward so the consumer class can mark the DB status as FAILED
            throw ex; 
            
        } finally {
            // This ALWAYS runs, ensuring your physical SIM card goes back to the queue
            simPoolManager.release(txn.getOperator(), sim);
            log.info("SIM {} released back to pool", sim.getSimName());
        }
    }
}