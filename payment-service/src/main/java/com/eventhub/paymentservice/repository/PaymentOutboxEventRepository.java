package com.eventhub.paymentservice.repository;

import com.eventhub.paymentservice.domain.PaymentOutboxEvent;
import com.eventhub.paymentservice.domain.PaymentOutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOutboxEventRepository extends JpaRepository<PaymentOutboxEvent, UUID> {
    List<PaymentOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(PaymentOutboxEventStatus status);
}
