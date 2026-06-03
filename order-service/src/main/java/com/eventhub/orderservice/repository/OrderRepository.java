package com.eventhub.orderservice.repository;

import com.eventhub.orderservice.domain.Order;
import com.eventhub.orderservice.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, Instant instant);
}

