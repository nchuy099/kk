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
    TopicExchange sagaExchange() {
        return new TopicExchange(RabbitTopics.SAGA_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange sagaDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.SAGA_DLX, true, false);
    }

    @Bean
    Queue orderCreatedQueue() {
        return queue(RabbitTopics.INVENTORY_ORDER_CREATED_QUEUE, RabbitTopics.INVENTORY_ORDER_CREATED_DLQ);
    }

    @Bean
    Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(sagaExchange).with(RabbitTopics.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    Queue orderConfirmedQueue() {
        return queue(RabbitTopics.INVENTORY_ORDER_CONFIRMED_QUEUE, RabbitTopics.INVENTORY_ORDER_CONFIRMED_DLQ);
    }

    @Bean
    Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(sagaExchange).with(RabbitTopics.ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    Queue orderCancelledQueue() {
        return queue(RabbitTopics.INVENTORY_ORDER_CANCELLED_QUEUE, RabbitTopics.INVENTORY_ORDER_CANCELLED_DLQ);
    }

    @Bean
    Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(sagaExchange).with(RabbitTopics.ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    Queue orderExpiredQueue() {
        return queue(RabbitTopics.INVENTORY_ORDER_EXPIRED_QUEUE, RabbitTopics.INVENTORY_ORDER_EXPIRED_DLQ);
    }

    @Bean
    Binding orderExpiredBinding(Queue orderExpiredQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderExpiredQueue).to(sagaExchange).with(RabbitTopics.ORDER_EXPIRED_ROUTING_KEY);
    }

    @Bean
    Queue orderRefundedQueue() {
        return queue(RabbitTopics.INVENTORY_ORDER_REFUNDED_QUEUE, RabbitTopics.INVENTORY_ORDER_REFUNDED_DLQ);
    }

    @Bean
    Binding orderRefundedBinding(Queue orderRefundedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderRefundedQueue).to(sagaExchange).with(RabbitTopics.ORDER_REFUNDED_ROUTING_KEY);
    }

    @Bean
    Queue orderCreatedDlq() {
        return QueueBuilder.durable(RabbitTopics.INVENTORY_ORDER_CREATED_DLQ).build();
    }

    @Bean
    Binding orderCreatedDlqBinding(Queue orderCreatedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCreatedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.INVENTORY_ORDER_CREATED_DLQ);
    }

    @Bean
    Queue orderConfirmedDlq() {
        return QueueBuilder.durable(RabbitTopics.INVENTORY_ORDER_CONFIRMED_DLQ).build();
    }

    @Bean
    Binding orderConfirmedDlqBinding(Queue orderConfirmedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderConfirmedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.INVENTORY_ORDER_CONFIRMED_DLQ);
    }

    @Bean
    Queue orderCancelledDlq() {
        return QueueBuilder.durable(RabbitTopics.INVENTORY_ORDER_CANCELLED_DLQ).build();
    }

    @Bean
    Binding orderCancelledDlqBinding(Queue orderCancelledDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCancelledDlq).to(sagaDeadLetterExchange).with(RabbitTopics.INVENTORY_ORDER_CANCELLED_DLQ);
    }

    @Bean
    Queue orderExpiredDlq() {
        return QueueBuilder.durable(RabbitTopics.INVENTORY_ORDER_EXPIRED_DLQ).build();
    }

    @Bean
    Binding orderExpiredDlqBinding(Queue orderExpiredDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderExpiredDlq).to(sagaDeadLetterExchange).with(RabbitTopics.INVENTORY_ORDER_EXPIRED_DLQ);
    }

    @Bean
    Queue orderRefundedDlq() {
        return QueueBuilder.durable(RabbitTopics.INVENTORY_ORDER_REFUNDED_DLQ).build();
    }

    @Bean
    Binding orderRefundedDlqBinding(Queue orderRefundedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderRefundedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.INVENTORY_ORDER_REFUNDED_DLQ);
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
