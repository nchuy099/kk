package com.eventhub.orderservice.repository;

import com.eventhub.orderservice.domain.OrderOutboxEvent;
import com.eventhub.orderservice.domain.OrderOutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, UUID> {
    List<OrderOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OrderOutboxEventStatus status);
}
