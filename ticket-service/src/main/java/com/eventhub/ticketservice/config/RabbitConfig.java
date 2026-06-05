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
    TopicExchange sagaExchange() {
        return new TopicExchange(RabbitTopics.SAGA_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange sagaDeadLetterExchange() {
        return new TopicExchange(RabbitTopics.SAGA_DLX, true, false);
    }

    @Bean
    Queue ticketIssueRequestedQueue() {
        return queue(RabbitTopics.TICKET_TICKET_ISSUE_REQUESTED_QUEUE, RabbitTopics.TICKET_TICKET_ISSUE_REQUESTED_DLQ);
    }

    @Bean
    Binding ticketIssueRequestedBinding(Queue ticketIssueRequestedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(ticketIssueRequestedQueue).to(sagaExchange).with(RabbitTopics.TICKET_ISSUE_REQUESTED_ROUTING_KEY);
    }

    @Bean
    Queue orderRefundedQueue() {
        return queue(RabbitTopics.TICKET_ORDER_REFUNDED_QUEUE, RabbitTopics.TICKET_ORDER_REFUNDED_DLQ);
    }

    @Bean
    Binding orderRefundedBinding(Queue orderRefundedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderRefundedQueue).to(sagaExchange).with(RabbitTopics.ORDER_REFUNDED_ROUTING_KEY);
    }

    @Bean
    Queue orderCancelledQueue() {
        return queue(RabbitTopics.TICKET_ORDER_CANCELLED_QUEUE, RabbitTopics.TICKET_ORDER_CANCELLED_DLQ);
    }

    @Bean
    Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(sagaExchange).with(RabbitTopics.ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    Queue ticketIssueRequestedDlq() {
        return QueueBuilder.durable(RabbitTopics.TICKET_TICKET_ISSUE_REQUESTED_DLQ).build();
    }

    @Bean
    Binding ticketIssueRequestedDlqBinding(Queue ticketIssueRequestedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(ticketIssueRequestedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.TICKET_TICKET_ISSUE_REQUESTED_DLQ);
    }

    @Bean
    Queue orderRefundedDlq() {
        return QueueBuilder.durable(RabbitTopics.TICKET_ORDER_REFUNDED_DLQ).build();
    }

    @Bean
    Binding orderRefundedDlqBinding(Queue orderRefundedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderRefundedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.TICKET_ORDER_REFUNDED_DLQ);
    }

    @Bean
    Queue orderCancelledDlq() {
        return QueueBuilder.durable(RabbitTopics.TICKET_ORDER_CANCELLED_DLQ).build();
    }

    @Bean
    Binding orderCancelledDlqBinding(Queue orderCancelledDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCancelledDlq).to(sagaDeadLetterExchange).with(RabbitTopics.TICKET_ORDER_CANCELLED_DLQ);
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
