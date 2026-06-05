package com.eventhub.inventoryservice.config;

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
    TopicExchange orderDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.ORDER_DLX, true, false);
    }

    @Bean
    Queue orderPaidQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", RabbitTopics.ORDER_DLX);
        arguments.put("x-dead-letter-routing-key", RabbitTopics.INVENTORY_ORDER_PAID_DLQ);
        return new Queue(RabbitTopics.INVENTORY_ORDER_PAID_QUEUE, true, false, false, arguments);
    }

    @Bean
    Binding orderPaidBinding(Queue orderPaidQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderPaidQueue).to(orderExchange).with(RabbitTopics.ORDER_PAID_ROUTING_KEY);
    }

    @Bean
    Queue orderPaidDlq() {
        return QueueBuilder.durable(RabbitTopics.INVENTORY_ORDER_PAID_DLQ).build();
    }

    @Bean
    Binding orderPaidDlqBinding(Queue orderPaidDlq, TopicExchange orderDeadLetterExchange) {
        return BindingBuilder.bind(orderPaidDlq).to(orderDeadLetterExchange).with(RabbitTopics.INVENTORY_ORDER_PAID_DLQ);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
