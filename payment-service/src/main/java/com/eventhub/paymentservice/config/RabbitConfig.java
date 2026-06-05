package com.eventhub.paymentservice.config;

import com.eventhub.common.messaging.RabbitTopics;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange sagaExchange() {
        return new TopicExchange(RabbitTopics.SAGA_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange sagaDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.SAGA_DLX, true, false);
    }

    @Bean
    Queue paymentRequestedQueue() {
        return queue(RabbitTopics.PAYMENT_PAYMENT_REQUESTED_QUEUE, RabbitTopics.PAYMENT_PAYMENT_REQUESTED_DLQ);
    }

    @Bean
    Binding paymentRequestedBinding(Queue paymentRequestedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(paymentRequestedQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_REQUESTED_ROUTING_KEY);
    }

    @Bean
    Queue orderExpiredQueue() {
        return queue(RabbitTopics.PAYMENT_ORDER_EXPIRED_QUEUE, RabbitTopics.PAYMENT_ORDER_EXPIRED_DLQ);
    }

    @Bean
    Binding orderExpiredBinding(Queue orderExpiredQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderExpiredQueue).to(sagaExchange).with(RabbitTopics.ORDER_EXPIRED_ROUTING_KEY);
    }

    @Bean
    Queue refundRequestedQueue() {
        return queue(RabbitTopics.PAYMENT_REFUND_REQUESTED_QUEUE, RabbitTopics.PAYMENT_REFUND_REQUESTED_DLQ);
    }

    @Bean
    Binding refundRequestedBinding(Queue refundRequestedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(refundRequestedQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_REFUND_REQUESTED_ROUTING_KEY);
    }

    @Bean
    Queue paymentRequestedDlq() {
        return QueueBuilder.durable(RabbitTopics.PAYMENT_PAYMENT_REQUESTED_DLQ).build();
    }

    @Bean
    Binding paymentRequestedDlqBinding(Queue paymentRequestedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(paymentRequestedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.PAYMENT_PAYMENT_REQUESTED_DLQ);
    }

    @Bean
    Queue orderExpiredDlq() {
        return QueueBuilder.durable(RabbitTopics.PAYMENT_ORDER_EXPIRED_DLQ).build();
    }

    @Bean
    Binding orderExpiredDlqBinding(Queue orderExpiredDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderExpiredDlq).to(sagaDeadLetterExchange).with(RabbitTopics.PAYMENT_ORDER_EXPIRED_DLQ);
    }

    @Bean
    Queue refundRequestedDlq() {
        return QueueBuilder.durable(RabbitTopics.PAYMENT_REFUND_REQUESTED_DLQ).build();
    }

    @Bean
    Binding refundRequestedDlqBinding(Queue refundRequestedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(refundRequestedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.PAYMENT_REFUND_REQUESTED_DLQ);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private Queue queue(String name, String dlqRoutingKey) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", RabbitTopics.SAGA_DLX);
        arguments.put("x-dead-letter-routing-key", dlqRoutingKey);
        return new Queue(name, true, false, false, arguments);
    }
}
