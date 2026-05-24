package com.bytebridges.anytop.domain.rabbitmq.producer;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.bytebridges.anytop.config.RabbitMQConfig;
import com.bytebridges.anytop.domain.transaction.dto.TopupMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class TelcoTopupMessageProducer {

	private final RabbitTemplate rabbitTemplate;

	public void send(TopupMessage message) {

		long start = System.currentTimeMillis();

		if (message.getMessageId() == null) {
			message.setMessageId(UUID.randomUUID().toString());
		}

		String queueName = getQueue(message.getOperator());

		try {

			rabbitTemplate.convertAndSend(queueName, message);

			long duration = System.currentTimeMillis() - start;

			log.info("Topup producer sent txnId={} messageId={} operator={} queue={} durationMs={}", message.getTxnId(),
					message.getMessageId(), message.getOperator(), queueName, duration);

		} catch (Exception ex) {

			long duration = System.currentTimeMillis() - start;

			log.error("Topup producer failed txnId={} messageId={} operator={} queue={} durationMs={}",
					message.getTxnId(), message.getMessageId(), message.getOperator(), queueName, duration, ex);

			throw ex;
		}
	}

	private String getQueue(String operator) {

		return switch (operator) {

		case "MPT" -> RabbitMQConfig.MPT_QUEUE;

		case "ATOM" -> RabbitMQConfig.ATOM_QUEUE;

		case "MYTEL" -> RabbitMQConfig.MYTEL_QUEUE;

		case "U9" -> RabbitMQConfig.U9_QUEUE;

		default -> throw new RuntimeException("Unsupported operator: " + operator);
		};
	}
}