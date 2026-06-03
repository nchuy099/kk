package com.eventhub.notificationservice.config;

import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange ticketExchange() {
        return new TopicExchange(RabbitTopics.TICKET_EXCHANGE, true, false);
    }

    @Bean
    Queue ticketIssuedQueue() {
        return new Queue("notification.ticket-issued", true);
    }

    @Bean
    Binding ticketIssuedBinding(Queue ticketIssuedQueue, TopicExchange ticketExchange) {
        return BindingBuilder.bind(ticketIssuedQueue).to(ticketExchange).with(RabbitTopics.TICKET_ISSUED_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

