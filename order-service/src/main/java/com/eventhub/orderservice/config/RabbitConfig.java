package com.eventhub.orderservice.config;

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
    TopicExchange paymentExchange() {
        return new TopicExchange(RabbitTopics.PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange(RabbitTopics.ORDER_EXCHANGE, true, false);
    }

    @Bean
    Queue paymentSucceededQueue() {
        return new Queue("order.payment-succeeded", true);
    }

    @Bean
    Binding paymentSucceededBinding(Queue paymentSucceededQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentSucceededQueue).to(paymentExchange).with(RabbitTopics.PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

