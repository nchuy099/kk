package com.eventhub.orderservice.config;

import com.eventhub.common.messaging.RabbitTopics;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

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
    TopicExchange orderDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.ORDER_DLX, true, false);
    }

    @Bean
    Queue paymentSucceededQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", RabbitTopics.ORDER_DLX);
        arguments.put("x-dead-letter-routing-key", RabbitTopics.ORDER_PAYMENT_SUCCEEDED_DLQ);
        return new Queue(RabbitTopics.ORDER_PAYMENT_SUCCEEDED_QUEUE, true, false, false, arguments);
    }

    @Bean
    Binding paymentSucceededBinding(Queue paymentSucceededQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentSucceededQueue).to(paymentExchange).with(RabbitTopics.PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    Queue paymentSucceededDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_PAYMENT_SUCCEEDED_DLQ).build();
    }

    @Bean
    Binding paymentSucceededDlqBinding(Queue paymentSucceededDlq, TopicExchange orderDeadLetterExchange) {
        return BindingBuilder.bind(paymentSucceededDlq).to(orderDeadLetterExchange).with(RabbitTopics.ORDER_PAYMENT_SUCCEEDED_DLQ);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
