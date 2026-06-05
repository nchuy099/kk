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
    TopicExchange sagaExchange() {
        return new TopicExchange(RabbitTopics.SAGA_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange sagaDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.SAGA_DLX, true, false);
    }

    @Bean
    Queue orderCompletedQueue() {
        return queue(RabbitTopics.NOTIFICATION_ORDER_COMPLETED_QUEUE, RabbitTopics.NOTIFICATION_ORDER_COMPLETED_DLQ);
    }

    @Bean
    Binding orderCompletedBinding(Queue orderCompletedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCompletedQueue).to(sagaExchange).with(RabbitTopics.ORDER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    Queue orderCancelledQueue() {
        return queue(RabbitTopics.NOTIFICATION_ORDER_CANCELLED_QUEUE, RabbitTopics.NOTIFICATION_ORDER_CANCELLED_DLQ);
    }

    @Bean
    Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(sagaExchange).with(RabbitTopics.ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    Queue orderRefundedQueue() {
        return queue(RabbitTopics.NOTIFICATION_ORDER_REFUNDED_QUEUE, RabbitTopics.NOTIFICATION_ORDER_REFUNDED_DLQ);
    }

    @Bean
    Binding orderRefundedBinding(Queue orderRefundedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderRefundedQueue).to(sagaExchange).with(RabbitTopics.ORDER_REFUNDED_ROUTING_KEY);
    }

    @Bean
    Queue orderCompensationFailedQueue() {
        return queue(RabbitTopics.NOTIFICATION_ORDER_COMPENSATION_FAILED_QUEUE, RabbitTopics.NOTIFICATION_ORDER_COMPENSATION_FAILED_DLQ);
    }

    @Bean
    Binding orderCompensationFailedBinding(Queue orderCompensationFailedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCompensationFailedQueue).to(sagaExchange).with(RabbitTopics.ORDER_COMPENSATION_FAILED_ROUTING_KEY);
    }

    @Bean
    Queue orderCompletedDlq() {
        return QueueBuilder.durable(RabbitTopics.NOTIFICATION_ORDER_COMPLETED_DLQ).build();
    }

    @Bean
    Binding orderCompletedDlqBinding(Queue orderCompletedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCompletedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.NOTIFICATION_ORDER_COMPLETED_DLQ);
    }

    @Bean
    Queue orderCancelledDlq() {
        return QueueBuilder.durable(RabbitTopics.NOTIFICATION_ORDER_CANCELLED_DLQ).build();
    }

    @Bean
    Binding orderCancelledDlqBinding(Queue orderCancelledDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCancelledDlq).to(sagaDeadLetterExchange).with(RabbitTopics.NOTIFICATION_ORDER_CANCELLED_DLQ);
    }

    @Bean
    Queue orderRefundedDlq() {
        return QueueBuilder.durable(RabbitTopics.NOTIFICATION_ORDER_REFUNDED_DLQ).build();
    }

    @Bean
    Binding orderRefundedDlqBinding(Queue orderRefundedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderRefundedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.NOTIFICATION_ORDER_REFUNDED_DLQ);
    }

    @Bean
    Queue orderCompensationFailedDlq() {
        return QueueBuilder.durable(RabbitTopics.NOTIFICATION_ORDER_COMPENSATION_FAILED_DLQ).build();
    }

    @Bean
    Binding orderCompensationFailedDlqBinding(Queue orderCompensationFailedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCompensationFailedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.NOTIFICATION_ORDER_COMPENSATION_FAILED_DLQ);
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
