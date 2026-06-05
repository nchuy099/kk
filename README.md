# Sports Event Ticketing Microservices Platform

A choreography-based Saga sports ticketing platform that simulates an authorized distributor for official tournament tickets. Fans browse competitions, create orders, trigger payment, and receive QR tickets through asynchronous service coordination.

## Services

- `event-service`
- `inventory-service`
- `order-service`
- `payment-service`
- `ticket-service`
- `notification-service`
- `user-service`
- `api-gateway`

## Current Scope

- Competition, stadium, event, and ticket category browsing
- Event-first order creation with `OrderCreatedEvent`
- Inventory reservation and oversell prevention with pessimistic locking
- Async payment creation and idempotent webhook processing
- Choreography Saga across order, inventory, payment, ticket, and notification services
- Refund compensation after ticket issue failure
- DLQ-enabled RabbitMQ consumers and outbox-based publishers
- Prometheus, Grafana, and Zipkin observability

## Saga Flow

1. `POST /api/orders` creates an order in `PENDING` and publishes `OrderCreatedEvent`.
2. `inventory-service` reserves ticket quota and publishes `InventoryReservedEvent` or `InventoryReserveFailedEvent`.
3. `order-service` reacts to `InventoryReservedEvent` and publishes `PaymentRequestedEvent`.
4. `payment-service` creates a local payment and publishes `PaymentCreatedEvent`.
5. Payment webhook publishes `PaymentSucceededEvent` or `PaymentFailedEvent`.
6. `order-service` publishes `OrderConfirmedEvent` and `TicketIssueRequestedEvent`.
7. `inventory-service` confirms the reservation, and `ticket-service` issues QR tickets.
8. `ticket-service` publishes `TicketIssuedEvent`, then `order-service` publishes `OrderCompletedEvent`.
9. `notification-service` reacts to final order events.

Compensation path:

- Inventory reserve fail -> `OrderCancelledEvent`
- Payment fail or timeout -> `OrderCancelledEvent` or `OrderExpiredEvent` -> inventory release
- Ticket issue fail after payment success -> `PaymentRefundRequestedEvent` -> `PaymentRefundedEvent` -> `OrderRefundedEvent`

## Demo IDs

- `competitionId`: `00000000-0000-0000-0000-000000000001`
- `stadiumId`: `00000000-0000-0000-0000-000000000010`
- `eventId`: `00000000-0000-0000-0000-000000000101`
- `Category 1 ticketCategoryId`: `11111111-1111-1111-1111-111111111111`
- `Category 2 ticketCategoryId`: `22222222-2222-2222-2222-222222222222`

## Run

```bash
docker compose up --build
```

## Sample Requests

Get a user token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8088/realms/event-hub/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=event-hub-cli' \
  -d 'grant_type=password' \
  -d 'username=user' \
  -d 'password=user' | jq -r .access_token)
```

Browse competitions:

```bash
curl http://localhost:8080/api/competitions
```

Create an order:

```bash
ORDER_ID=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "00000000-0000-0000-0000-000000000101",
    "items": [
      {
        "ticketCategoryId": "11111111-1111-1111-1111-111111111111",
        "quantity": 2
      }
    ]
  }' | jq -r .id)
```

Poll until payment exists:

```bash
curl http://localhost:8080/api/payments/by-order/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

Mock a payment webhook:

```bash
curl -X POST http://localhost:8080/api/payments/webhook \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{
    \"providerEventId\": \"evt-$ORDER_ID\",
    \"transactionId\": \"txn-$ORDER_ID\",
    \"orderId\": \"$ORDER_ID\",
    \"status\": \"SUCCEEDED\",
    \"amount\": 1000000
  }"
```

Get order tickets:

```bash
curl http://localhost:8080/api/orders/$ORDER_ID/tickets \
  -H "Authorization: Bearer $TOKEN"
```

## Ports

- `api-gateway`: `8080`
- `event-service`: `8081`
- `inventory-service`: `8082`
- `order-service`: `8083`
- `payment-service`: `8084`
- `ticket-service`: `8085`
- `notification-service`: `8086`
- `user-service`: `8087`
- Keycloak: `8088`
- Prometheus: `9090`
- Zipkin: `9411`
- Grafana: `3000`
- RabbitMQ UI: `15672`

## k6 Scenarios

- `k6/event-browsing.js`: competition/event/category browsing load
- `k6/concurrent-booking.js`: concurrent order creation on one ticket category
- `k6/webhook-spike.js`: duplicate payment webhook handling after async payment creation
