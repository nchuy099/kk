package com.eventhub.orderservice.config;

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
    Queue inventoryReservedQueue() {
        return queue(RabbitTopics.ORDER_INVENTORY_RESERVED_QUEUE, RabbitTopics.ORDER_INVENTORY_RESERVED_DLQ);
    }

    @Bean
    Binding inventoryReservedBinding(Queue inventoryReservedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(inventoryReservedQueue).to(sagaExchange).with(RabbitTopics.INVENTORY_RESERVED_ROUTING_KEY);
    }

    @Bean
    Queue inventoryReserveFailedQueue() {
        return queue(RabbitTopics.ORDER_INVENTORY_RESERVE_FAILED_QUEUE, RabbitTopics.ORDER_INVENTORY_RESERVE_FAILED_DLQ);
    }

    @Bean
    Binding inventoryReserveFailedBinding(Queue inventoryReserveFailedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(inventoryReserveFailedQueue).to(sagaExchange).with(RabbitTopics.INVENTORY_RESERVE_FAILED_ROUTING_KEY);
    }

    @Bean
    Queue paymentCreatedQueue() {
        return queue(RabbitTopics.ORDER_PAYMENT_CREATED_QUEUE, RabbitTopics.ORDER_PAYMENT_CREATED_DLQ);
    }

    @Bean
    Binding paymentCreatedBinding(Queue paymentCreatedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(paymentCreatedQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_CREATED_ROUTING_KEY);
    }

    @Bean
    Queue paymentSucceededQueue() {
        return queue(RabbitTopics.ORDER_PAYMENT_SUCCEEDED_QUEUE, RabbitTopics.ORDER_PAYMENT_SUCCEEDED_DLQ);
    }

    @Bean
    Binding paymentSucceededBinding(Queue paymentSucceededQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(paymentSucceededQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_SUCCEEDED_ROUTING_KEY);
    }

    @Bean
    Queue paymentFailedQueue() {
        return queue(RabbitTopics.ORDER_PAYMENT_FAILED_QUEUE, RabbitTopics.ORDER_PAYMENT_FAILED_DLQ);
    }

    @Bean
    Binding paymentFailedBinding(Queue paymentFailedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    Queue ticketIssuedQueue() {
        return queue(RabbitTopics.ORDER_TICKET_ISSUED_QUEUE, RabbitTopics.ORDER_TICKET_ISSUED_DLQ);
    }

    @Bean
    Binding ticketIssuedBinding(Queue ticketIssuedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(ticketIssuedQueue).to(sagaExchange).with(RabbitTopics.TICKET_ISSUED_ROUTING_KEY);
    }

    @Bean
    Queue ticketIssueFailedQueue() {
        return queue(RabbitTopics.ORDER_TICKET_ISSUE_FAILED_QUEUE, RabbitTopics.ORDER_TICKET_ISSUE_FAILED_DLQ);
    }

    @Bean
    Binding ticketIssueFailedBinding(Queue ticketIssueFailedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(ticketIssueFailedQueue).to(sagaExchange).with(RabbitTopics.TICKET_ISSUE_FAILED_ROUTING_KEY);
    }

    @Bean
    Queue paymentRefundedQueue() {
        return queue(RabbitTopics.ORDER_PAYMENT_REFUNDED_QUEUE, RabbitTopics.ORDER_PAYMENT_REFUNDED_DLQ);
    }

    @Bean
    Binding paymentRefundedBinding(Queue paymentRefundedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(paymentRefundedQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_REFUNDED_ROUTING_KEY);
    }

    @Bean
    Queue paymentRefundFailedQueue() {
        return queue(RabbitTopics.ORDER_PAYMENT_REFUND_FAILED_QUEUE, RabbitTopics.ORDER_PAYMENT_REFUND_FAILED_DLQ);
    }

    @Bean
    Binding paymentRefundFailedBinding(Queue paymentRefundFailedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(paymentRefundFailedQueue).to(sagaExchange).with(RabbitTopics.PAYMENT_REFUND_FAILED_ROUTING_KEY);
    }

    @Bean
    Queue reservationExpiredQueue() {
        return queue(RabbitTopics.ORDER_RESERVATION_EXPIRED_QUEUE, RabbitTopics.ORDER_RESERVATION_EXPIRED_DLQ);
    }

    @Bean
    Binding reservationExpiredBinding(Queue reservationExpiredQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(reservationExpiredQueue).to(sagaExchange).with(RabbitTopics.RESERVATION_EXPIRED_ROUTING_KEY);
    }

    @Bean
    Queue inventoryReservedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_INVENTORY_RESERVED_DLQ).build();
    }

    @Bean
    Binding inventoryReservedDlqBinding(Queue inventoryReservedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(inventoryReservedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_INVENTORY_RESERVED_DLQ);
    }

    @Bean
    Queue inventoryReserveFailedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_INVENTORY_RESERVE_FAILED_DLQ).build();
    }

    @Bean
    Binding inventoryReserveFailedDlqBinding(Queue inventoryReserveFailedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(inventoryReserveFailedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_INVENTORY_RESERVE_FAILED_DLQ);
    }

    @Bean
    Queue paymentCreatedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_PAYMENT_CREATED_DLQ).build();
    }

    @Bean
    Binding paymentCreatedDlqBinding(Queue paymentCreatedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(paymentCreatedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_PAYMENT_CREATED_DLQ);
    }

    @Bean
    Queue paymentSucceededDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_PAYMENT_SUCCEEDED_DLQ).build();
    }

    @Bean
    Binding paymentSucceededDlqBinding(Queue paymentSucceededDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(paymentSucceededDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_PAYMENT_SUCCEEDED_DLQ);
    }

    @Bean
    Queue paymentFailedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_PAYMENT_FAILED_DLQ).build();
    }

    @Bean
    Binding paymentFailedDlqBinding(Queue paymentFailedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(paymentFailedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_PAYMENT_FAILED_DLQ);
    }

    @Bean
    Queue ticketIssuedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_TICKET_ISSUED_DLQ).build();
    }

    @Bean
    Binding ticketIssuedDlqBinding(Queue ticketIssuedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(ticketIssuedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_TICKET_ISSUED_DLQ);
    }

    @Bean
    Queue ticketIssueFailedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_TICKET_ISSUE_FAILED_DLQ).build();
    }

    @Bean
    Binding ticketIssueFailedDlqBinding(Queue ticketIssueFailedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(ticketIssueFailedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_TICKET_ISSUE_FAILED_DLQ);
    }

    @Bean
    Queue paymentRefundedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_PAYMENT_REFUNDED_DLQ).build();
    }

    @Bean
    Binding paymentRefundedDlqBinding(Queue paymentRefundedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(paymentRefundedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_PAYMENT_REFUNDED_DLQ);
    }

    @Bean
    Queue paymentRefundFailedDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_PAYMENT_REFUND_FAILED_DLQ).build();
    }

    @Bean
    Binding paymentRefundFailedDlqBinding(Queue paymentRefundFailedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(paymentRefundFailedDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_PAYMENT_REFUND_FAILED_DLQ);
    }

    @Bean
    Queue reservationExpiredDlq() {
        return QueueBuilder.durable(RabbitTopics.ORDER_RESERVATION_EXPIRED_DLQ).build();
    }

    @Bean
    Binding reservationExpiredDlqBinding(Queue reservationExpiredDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(reservationExpiredDlq).to(sagaDeadLetterExchange).with(RabbitTopics.ORDER_RESERVATION_EXPIRED_DLQ);
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
