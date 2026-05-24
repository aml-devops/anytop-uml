package com.bytebridges.anytop.domain.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.bytebridges.anytop.config.RabbitMQConfig;
import com.bytebridges.anytop.domain.rabbitmq.service.GsmWorker;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;
import com.bytebridges.anytop.domain.transaction.repository.TransactionRepository;

/**
 * RabbitMQ consumer for U9 top-up transactions.
 *
 * Consumes top-up messages and dispatches asynchronous GSM processing workers.
 */
@Component
public class MytelTopupConsumer extends AbstractTopupConsumer {
	public MytelTopupConsumer(TransactionRepository transactionRepository, GsmWorker gsmWorker,
			TaskExecutor gsmExecutor) {
		super(transactionRepository, gsmWorker, gsmExecutor);
	}

	@RabbitListener(queues = RabbitMQConfig.MYTEL_QUEUE, concurrency = "5")
	public void process(TopupMessage message) {
		handle("MYTEL", message);
	}	
}