# Event Hub

Event Booking Microservices Platform built as an MVP demo.

## What is included

- `event-service`
- `inventory-service`
- `order-service`
- `payment-service`
- `user-service`
- `ticket-service`
- `notification-service`
- `api-gateway`
- PostgreSQL per service
- RabbitMQ
- Prometheus
- Grafana
- Zipkin
- Keycloak
- k6 scripts for load and concurrency tests

## Current scope

- Event browsing
- Reservation-based booking
- Oversell prevention with database locking
- Mock payment webhook with idempotency
- User profile management backed by Keycloak identity
- Async ticket issuing through RabbitMQ
- API Gateway with Keycloak JWT validation, rate limiting, and request logging
- Distributed tracing with Zipkin
- RabbitMQ dead-letter queues for failed consumers
- Outbox-based event publishing for payment, order, and ticket workflows
- Notification logging
- Event API includes `availableQuantity` for each ticket type

## Run with Docker

The repository is designed to run through Docker Compose. Each service builds in a container that has Gradle available, so local Gradle installation is not required.

```bash
docker compose up --build
```

## Demo flow

1. Query events through the `api-gateway`.
2. Create an order through the `api-gateway`.
3. Trigger a mock payment webhook through the `api-gateway`.
4. Observe `order-service` marking the order paid.
5. Observe `ticket-service` issuing tickets.
6. Run k6 booking and webhook spike tests.

## Demo IDs

The services seed the same fixed IDs so the demo flow stays reproducible:

- `eventId`: `00000000-0000-0000-0000-000000000101`
- `VIP ticketTypeId`: `11111111-1111-1111-1111-111111111111`
- `GENERAL ticketTypeId`: `22222222-2222-2222-2222-222222222222`

## Sample request

Use a token from the Gateway Auth section below.

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "ticketTypeId": "11111111-1111-1111-1111-111111111111",
    "quantity": 2
  }'
```

## Ports

- `event-service`: `8081`
- `inventory-service`: `8082`
- `order-service`: `8083`
- `payment-service`: `8084`
- `ticket-service`: `8085`
- `notification-service`: `8086`
- `user-service`: `8087`
- `api-gateway`: `8080`
- RabbitMQ UI: `15672`
- Keycloak: `8088`
- Zipkin: `9411`
- Prometheus: `9090`
- Grafana: `3000`

## Gateway Auth

The gateway validates Keycloak tokens.

Keycloak admin console: `http://localhost:8088`

Login: `admin / admin`

Demo token for `user`:

```bash
TOKEN=$(curl -s -X POST http://localhost:8088/realms/event-hub/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=event-hub-cli' \
  -d 'grant_type=password' \
  -d 'username=user' \
  -d 'password=user' | jq -r .access_token)

curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "ticketTypeId": "11111111-1111-1111-1111-111111111111",
    "quantity": 2
  }'
```

User profile from the authenticated Keycloak token:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

Admin token for admin routes:

```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8088/realms/event-hub/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=event-hub-cli' \
  -d 'grant_type=password' \
  -d 'username=admin' \
  -d 'password=admin' | jq -r .access_token)

curl -X POST http://localhost:8080/api/admin/events \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "New Event",
    "description": "Created via Keycloak admin token",
    "startTime": "2026-06-10T09:00:00Z",
    "saleStartTime": "2026-06-05T09:00:00Z",
    "saleEndTime": "2026-06-09T09:00:00Z",
    "venue": "Main Hall"
  }'
```

Create a user through `user-service` and Keycloak Admin API:

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "new-user",
    "email": "new-user@example.com",
    "password": "password",
    "fullName": "New User",
    "phone": "0900000000"
  }'
```

## Grafana

- Login: `admin / admin`
- Datasource: `Prometheus`
- Dashboard: `Event Hub Overview`
- RabbitMQ queue depth panel uses RabbitMQ Prometheus metrics on port `15692`
- RabbitMQ is configured with `prometheus.return_per_object_metrics = true` so queue-level metrics are available on `/metrics`.
- If RabbitMQ panels look empty, verify Prometheus can query:
```promql
rabbitmq_queue_messages
```

## k6 Test Scenarios

The repository includes three k6 scripts under `k6/`.

### 1. Event Browsing Load

File: `k6/event-browsing.js`

What it tests:
- High read traffic on `GET /events`
- Basic latency and error-rate behavior under gradual load

Load profile:
- Ramp to `100 VUs` in `2m`
- Ramp to `300 VUs` in `3m`
- Ramp down to `0` in `2m`

Expected result:
- `GET /events` stays `200 OK`
- p95 latency remains stable under increasing read load
- No increase in 5xx errors

Run:
```bash
k6 run k6/event-browsing.js
```

### 2. Concurrent Booking

File: `k6/concurrent-booking.js`

What it tests:
- Concurrent order creation against the same ticket type
- Reservation handling under contention
- Oversell prevention in `inventory-service`

Load profile:
- `1000 VUs`
- `30s`

Expected result:
- Successful orders return `200` or `201`
- Reservation conflicts return `409`
- Inventory never goes negative
- Total sold + reserved never exceeds available stock

Run:
```bash
k6 run k6/concurrent-booking.js
```

### 3. Payment Webhook Spike

File: `k6/webhook-spike.js`

What it tests:
- Burst traffic on `POST /payments/webhook`
- Duplicate webhook handling
- Payment idempotency and downstream event fan-out

Load profile:
- `200 VUs`
- `20s`

Expected result:
- Webhook requests return `200`
- Duplicate `providerEventId` values do not cause double payment
- Ticket issuance happens once per paid order
- Notification is emitted once per ticket issuance event

Run:
```bash
k6 run k6/webhook-spike.js
```

### Notes

- Run `docker compose up -d` before k6.
- The scripts call the gateway on `http://localhost:8080`.
- Protected k6 scenarios request a token from Keycloak on `http://localhost:8088`.
- For a performance report, record:
  - p95 latency
  - error rate
  - successful vs rejected booking count
  - queue depth in Grafana during webhook spike
