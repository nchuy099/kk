package com.eventhub.ticketservice.config;

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
    TopicExchange orderExchange() {
        return new TopicExchange(RabbitTopics.ORDER_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange ticketExchange() {
        return new TopicExchange(RabbitTopics.TICKET_EXCHANGE, true, false);
    }

    @Bean
    Queue orderPaidQueue() {
        return new Queue("ticket.order-paid", true);
    }

    @Bean
    Binding orderPaidBinding(Queue orderPaidQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderPaidQueue).to(orderExchange).with(RabbitTopics.ORDER_PAID_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

