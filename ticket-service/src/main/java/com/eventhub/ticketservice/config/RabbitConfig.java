package com.eventhub.ticketservice.config;

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
    TopicExchange orderExchange() {
        return new TopicExchange(RabbitTopics.ORDER_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange ticketExchange() {
        return new TopicExchange(RabbitTopics.TICKET_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange ticketDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.TICKET_DLX, true, false);
    }

    @Bean
    Queue orderConfirmedQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", RabbitTopics.TICKET_DLX);
        arguments.put("x-dead-letter-routing-key", RabbitTopics.TICKET_ORDER_CONFIRMED_DLQ);
        return new Queue(RabbitTopics.TICKET_ORDER_CONFIRMED_QUEUE, true, false, false, arguments);
    }

    @Bean
    Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(orderExchange).with(RabbitTopics.ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    Queue orderConfirmedDlq() {
        return QueueBuilder.durable(RabbitTopics.TICKET_ORDER_CONFIRMED_DLQ).build();
    }

    @Bean
    Binding orderConfirmedDlqBinding(Queue orderConfirmedDlq, TopicExchange ticketDeadLetterExchange) {
        return BindingBuilder.bind(orderConfirmedDlq).to(ticketDeadLetterExchange).with(RabbitTopics.TICKET_ORDER_CONFIRMED_DLQ);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
