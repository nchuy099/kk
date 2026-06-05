# Sports Event Ticketing Microservices Platform

A microservices-based sports event ticketing platform that simulates an authorized ticket distributor for tournaments and stadium matches. Fans can browse competitions, select events, reserve official tickets, complete payment, and receive QR tickets.

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

- Competition and event browsing
- Stadium and ticket category management
- Temporary ticket reservation with expiration
- Oversell prevention with pessimistic locking
- Mock payment webhook with idempotency
- Async QR ticket issuing through RabbitMQ
- API Gateway with Keycloak JWT validation, rate limiting, and request logging
- Prometheus, Grafana, and Zipkin observability

## Demo Flow

1. Fan queries competitions and events through `api-gateway`.
2. Fan selects an event and ticket category.
3. Fan creates an order and inventory is reserved temporarily.
4. Payment service receives a mock success webhook.
5. Order becomes `CONFIRMED`.
6. Inventory moves reserved tickets to sold.
7. Ticket service issues QR tickets.
8. Notification service logs ticket delivery.

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
curl -X POST http://localhost:8080/api/orders \
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
  }'
```

Mock a payment webhook:

```bash
curl -X POST http://localhost:8080/api/payments/webhook \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "providerEventId": "evt-order-001",
    "transactionId": "txn-order-001",
    "orderId": "REPLACE_WITH_ORDER_ID",
    "status": "SUCCEEDED",
    "amount": 1000000
  }'
```

Get order tickets:

```bash
curl http://localhost:8080/api/orders/REPLACE_WITH_ORDER_ID/tickets \
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
- `k6/webhook-spike.js`: duplicate payment webhook handling
