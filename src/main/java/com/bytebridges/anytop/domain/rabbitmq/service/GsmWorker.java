package com.bytebridges.anytop.domain.rabbitmq.service;

import org.springframework.stereotype.Service;

import com.bytebridges.anytop.domain.eloadengine.router.UssdTopupEngine;
import com.bytebridges.anytop.domain.simcard.entity.SimCard;
import com.bytebridges.anytop.domain.simcard.repository.SimCardRepository;
import com.bytebridges.anytop.domain.simcard.service.SimPoolManager;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.enums.Operator;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GsmWorker {

	private final TransactionRepository transactionRepository;
	private final SimCardRepository simCardRepository;
	private final UssdTopupEngine ussdTopupEngine;
	private final SimPoolManager simPoolManager;

	public void execute(Long txnId) {

		Transaction txn = transactionRepository.findById(txnId)
				.orElseThrow(() -> new IllegalStateException("Transaction not found txnId=" + txnId));

		SimCard sim = null;
		String operator = txn.getOperator();
		try {
			/**
			 * 1 application instance ONLY But if later you scale horizontally: App-1,
			 * App-2, App-3
			 */
			// =========================
			// ACQUIRE SIM
			// =========================
			sim = simPoolManager.acquire(operator);

			// =========================
			// EXECUTE USSD TOPUP
			// =========================
			TxnStatus result = ussdTopupEngine.route(Operator.from(operator), txn.getId(), sim.getSimName(),
					sim.getPassword(), txn.getPhoneNumber(), String.valueOf(txn.getAmount()));

			// =========================
			// SIM BALANCE DEDUCTION
			// =========================
			/**
			if (result == TxnStatus.SUCCESS) {
				int updated = simCardRepository.deductBalance(sim.getId(), txn.getAmount());
				if (updated == 0) {
					log.error("SIM balance deduction failed simId={} amount={}", sim.getId(), txn.getAmount());
					throw new IllegalStateException("Insufficient SIM balance");
				}
			}*/

			// =========================
			// COMPLETE TRANSACTION
			// =========================
			transactionRepository.completeTransaction(txn.getId(), result.name(), sim.getId(), sim.getSimName());
			log.info("Topup done txnId={} operator={} simId={} result={}", txnId, operator, sim.getId(), result);
		} catch (Exception ex) {
			transactionRepository.markSystemFailed(txn.getId());
			log.error("Topup error txnId={} operator={} errorType={}", txnId, operator, ex.getClass().getSimpleName(), ex);
		} finally {
			if (sim != null) {
				simPoolManager.release(operator, sim);
			}
		}
	}
}