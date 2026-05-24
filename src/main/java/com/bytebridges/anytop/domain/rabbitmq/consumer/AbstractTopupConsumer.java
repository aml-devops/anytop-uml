package com.bytebridges.anytop.domain.rabbitmq.consumer;

import org.springframework.core.task.TaskExecutor;

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
	protected final TaskExecutor gsmExecutor;

	protected void handle(String operator, TopupMessage message) {
		long start = System.currentTimeMillis();

		try {
			log.info("{} message received txnId={} messageId={}", operator, message.getTxnId(), message.getMessageId());

			Transaction txn = transactionRepository.findByMessageId(message.getMessageId()).orElseThrow(
					() -> new IllegalStateException("Transaction not found messageId=" + message.getMessageId()));

			if (txn.getStatus() == TxnStatus.PROCESSING || txn.getStatus() == TxnStatus.SUCCESS) {
				log.warn("{} duplicate skipped txnId={} status={}", operator, txn.getId(), txn.getStatus());
				return;
			}

			Long txnId = txn.getId();
			int updated = transactionRepository.markProcessing(txnId);
			if (updated == 0) {
				log.warn("{} processing skipped txnId={} reason=ALREADY_PROCESSING", operator, txnId);
				return;
			}

			gsmExecutor.execute(() -> {
				try {
					gsmWorker.execute(txnId);
				} catch (Exception ex) {
					log.error("{} worker failed txnId={} errorType={}", operator, txnId, ex.getClass().getSimpleName(), ex);
				}
			});

			log.info("{} worker dispatched txnId={} durationMs={}", operator, txnId, System.currentTimeMillis() - start);
		} catch (Exception ex) {
			log.error("{} consumer error txnId={} errorType={}", operator, message.getTxnId(), ex.getClass().getSimpleName(), ex);
		}
	}
}
