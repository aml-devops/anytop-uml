package com.bytebridges.anytop.domain.transaction.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.bytebridges.anytop.domain.rabbitmq.producer.TelcoTopupMessageProducer;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;
import com.bytebridges.anytop.domain.transaction.dto.TopupResponseDto;
import com.bytebridges.anytop.domain.transaction.entity.Transaction;
import com.bytebridges.anytop.domain.transaction.enums.TxnStatus;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCommandService {
	private final TransactionRepository transactionRepository;
	private final TelcoTopupMessageProducer producer;

	// =========================
	// CREATE TOPUP (API ENTRY)
	// =========================
	@Transactional
	public TopupResponseDto createTopup(String operator, String phone, Integer amount) {

		Transaction txn = new Transaction();

		txn.setPhoneNumber(phone);
		txn.setAmount(amount);
		txn.setOperator(operator);
		txn.setStatus(TxnStatus.QUEUED);
		txn.setMessageId(UUID.randomUUID().toString());
		txn.setCreatedAt(LocalDateTime.now());

		transactionRepository.save(txn);

		//log.info("TOPUP_CREATED txnId={} operator={} amount={}", txn.getId(), operator, amount);

		// IMPORTANT:
		// send RabbitMQ message AFTER COMMIT
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {

				producer.send(new TopupMessage(txn.getId(), operator, txn.getMessageId()));

				//log.info("TOPUP_QUEUED txnId={} operator={}", txn.getId(), operator);
			}
		});

		return toResponse(txn);
	}

	public TopupResponseDto toResponse(Transaction txn) {

		return TopupResponseDto.builder().txnId(txn.getId()).status(txn.getStatus().name())
				.message("Topup request accepted and queued").operator(txn.getOperator())
				.phoneNumber(txn.getPhoneNumber()).amount(txn.getAmount()).messageId(txn.getMessageId())
				.createdAt(txn.getCreatedAt()).build();
	}
}