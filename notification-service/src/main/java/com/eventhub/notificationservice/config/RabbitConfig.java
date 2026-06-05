package com.eventhub.notificationservice.config;

import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange ticketExchange() {
        return new TopicExchange(RabbitTopics.TICKET_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange notificationDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.NOTIFICATION_DLX, true, false);
    }

    @Bean
    Queue ticketIssuedQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", RabbitTopics.NOTIFICATION_DLX);
        arguments.put("x-dead-letter-routing-key", RabbitTopics.NOTIFICATION_TICKET_ISSUED_DLQ);
        return new Queue(RabbitTopics.NOTIFICATION_TICKET_ISSUED_QUEUE, true, false, false, arguments);
    }

    @Bean
    Binding ticketIssuedBinding(Queue ticketIssuedQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(ticketIssuedQueue).to(ticketExchange).with(RabbitTopics.TICKET_ISSUED_ROUTING_KEY);
    }

    @Bean
    Queue ticketIssuedDlq() {
        return QueueBuilder.durable(RabbitTopics.NOTIFICATION_TICKET_ISSUED_DLQ).build();
    }

    @Bean
    Binding ticketIssuedDlqBinding(Queue ticketIssuedDlq, TopicExchange notificationDeadLetterExchange) {
        return BindingBuilder.bind(ticketIssuedDlq).to(notificationDeadLetterExchange).with(RabbitTopics.NOTIFICATION_TICKET_ISSUED_DLQ);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
