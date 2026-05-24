package com.bytebridges.anytop.domain.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.bytebridges.anytop.config.RabbitMQConfig;
import com.bytebridges.anytop.domain.rabbitmq.service.GsmWorker;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

/**
 * RabbitMQ consumer for ATOM top-up transactions.
 *
 * Consumes top-up messages and dispatches asynchronous GSM processing workers.
 */
@Component
public class AtomTopupConsumer extends AbstractTopupConsumer {
	public AtomTopupConsumer(TransactionRepository transactionRepository, GsmWorker gsmWorker,
			TaskExecutor gsmExecutor) {
		super(transactionRepository, gsmWorker, gsmExecutor);
	}

	@RabbitListener(queues = RabbitMQConfig.ATOM_QUEUE, concurrency = "5")
	public void process(TopupMessage message) {
		handle("ATOM", message);
	}
}