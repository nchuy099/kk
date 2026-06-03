# Event Hub

Event Booking Microservices Platform built as an MVP demo.

## What is included

- `event-service`
- `inventory-service`
- `order-service`
- `payment-service`
- `ticket-service`
- `notification-service`
- PostgreSQL per service
- RabbitMQ
- Prometheus
- Grafana
- k6 scripts for load and concurrency tests

## Current scope

- Event browsing
- Reservation-based booking
- Oversell prevention with database locking
- Mock payment webhook with idempotency
- Async ticket issuing through RabbitMQ
- Notification logging
- Event API includes `availableQuantity` for each ticket type

## Run with Docker

The repository is designed to run through Docker Compose. Each service builds in a container that has Gradle available, so local Gradle installation is not required.

```bash
docker compose up --build
```

## Demo flow

1. Query events from `event-service`.
2. Create an order through `order-service`.
3. Trigger a mock payment webhook.
4. Observe `order-service` marking the order paid.
5. Observe `ticket-service` issuing tickets.
6. Run k6 booking and webhook spike tests.

## Demo IDs

The services seed the same fixed IDs so the demo flow stays reproducible:

- `eventId`: `00000000-0000-0000-0000-000000000101`
- `VIP ticketTypeId`: `11111111-1111-1111-1111-111111111111`
- `GENERAL ticketTypeId`: `22222222-2222-2222-2222-222222222222`

## Sample request

```bash
curl -X POST http://localhost:8083/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "user-1",
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
- RabbitMQ UI: `15672`
- Prometheus: `9090`
- Grafana: `3000`

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
- The scripts call services on localhost:
  - event browsing: `http://localhost:8081`
  - booking: `http://localhost:8083`
  - payments: `http://localhost:8084`
- For a performance report, record:
  - p95 latency
  - error rate
  - successful vs rejected booking count
  - queue depth in Grafana during webhook spike
