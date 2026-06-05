package com.eventhub.notificationservice.service;

import com.eventhub.common.events.v1.OrderCancelledEventV1;
import com.eventhub.common.events.v1.OrderCompletedEventV1;
import com.eventhub.common.events.v1.OrderCompensationFailedEventV1;
import com.eventhub.common.events.v1.OrderRefundedEventV1;
import com.eventhub.common.messaging.RabbitTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SagaNotificationListener {
    @RabbitListener(queues = RabbitTopics.NOTIFICATION_ORDER_COMPLETED_QUEUE)
    public void onOrderCompleted(OrderCompletedEventV1 event) {
        log.info("Sending completed booking notification for order {}", event.orderId());
    }

    @RabbitListener(queues = RabbitTopics.NOTIFICATION_ORDER_CANCELLED_QUEUE)
    public void onOrderCancelled(OrderCancelledEventV1 event) {
        log.info("Sending cancelled booking notification for order {} because {}", event.orderId(), event.reason());
    }

    @RabbitListener(queues = RabbitTopics.NOTIFICATION_ORDER_REFUNDED_QUEUE)
    public void onOrderRefunded(OrderRefundedEventV1 event) {
        log.info("Sending refunded booking notification for order {} because {}", event.orderId(), event.reason());
    }

    @RabbitListener(queues = RabbitTopics.NOTIFICATION_ORDER_COMPENSATION_FAILED_QUEUE)
    public void onOrderCompensationFailed(OrderCompensationFailedEventV1 event) {
        log.warn("Sending manual review notification for order {} because {}", event.orderId(), event.reason());
    }
}
