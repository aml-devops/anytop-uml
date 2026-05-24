package com.bytebridges.anytop.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.bytebridges.anytop.domain.rabbitmq.producer.TelcoTopupMessageProducer;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchPublisherScheduler {

	private final TransactionRepository transactionRepository;
	private final TelcoTopupMessageProducer producer;

	@Scheduled(fixedDelay = 2000)
	@Transactional
	public void publishQueuedTransactions() {

		List<Transaction> txns = transactionRepository.findBatchForPublishing(100);

		for (Transaction txn : txns) {

			try {

				// publish AFTER COMMIT
				/**
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

					@Override
					public void afterCommit() {

						producer.send(new TopupMessage(txn.getId(), txn.getOperator(), txn.getMessageId()));

						log.info("BATCH_QUEUED txnId={} operator={}", txn.getId(), txn.getOperator());
					}
				});*/
				
				producer.send(new TopupMessage(txn.getId(), txn.getOperator(), txn.getMessageId()));

			} catch (Exception ex) {

				log.error("BATCH_PUBLISH_FAILED txnId={} error={}", txn.getId(), ex.getMessage(), ex);
			}
		}
	}
}