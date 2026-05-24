package com.bytebridges.anytop.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class RabbitMQConfig {

    public static final String MPT_QUEUE = "mpt.queue";
    public static final String ATOM_QUEUE = "atom.queue";
    public static final String MYTEL_QUEUE = "mytel.queue";
    public static final String U9_QUEUE = "u9.queue";

    @Bean
    public Queue mptQueue() {
        return new Queue(MPT_QUEUE, true);
    }

    @Bean
    public Queue atomQueue() {
        return new Queue(ATOM_QUEUE, true);
    }

    @Bean
    public Queue mytelQueue() {
        return new Queue(MYTEL_QUEUE, true);
    }

    @Bean
    public Queue u9Queue() {
        return new Queue(U9_QUEUE, true);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
    	return new Jackson2JsonMessageConverter();
    }
}


