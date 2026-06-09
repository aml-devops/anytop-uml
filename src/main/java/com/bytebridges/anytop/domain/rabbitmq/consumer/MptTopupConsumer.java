package com.bytebridges.anytop.domain.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.bytebridges.anytop.config.RabbitMQConfig;
import com.bytebridges.anytop.domain.rabbitmq.service.GsmWorker;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

/**
 * RabbitMQ consumer for MPT top-up transactions.
 *
 * Consumes top-up messages and dispatches asynchronous GSM processing workers.
 */
@Component
public class MptTopupConsumer extends AbstractTopupConsumer {

	public MptTopupConsumer(TransactionRepository transactionRepository, GsmWorker gsmWorker) {
		super(transactionRepository, gsmWorker);
	}

	@RabbitListener(queues = RabbitMQConfig.MPT_QUEUE, concurrency = "5")
	public void process(TopupMessage message) {
		handle("MPT", message);
	}
}