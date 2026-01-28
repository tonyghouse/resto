# Resto – Restaurant Order Management Platform

This repo contains the backend for a restaurant order system that works across multiple branches. The goal here is not to over-design, but still keep things close to how real production systems are built, specially ones that deal with payments and trust sensitive data.

The system is built as a **modular monolith**, but services are still isolated at a logical level. Everything runs locally using Docker Compose and can later be deployed to cloud without major changes.

---

## Architecture Overview

Right now the platform has two Spring Boot services.

### Restaurant Service
This service handles:
- Menu management (time based menus, combos, etc)
- Order creation and order lifecycle
- Price and tax calculation
- Validation and basic authorization logic

### Payment Service
This service is responsible for:
- Synchronous payment processing
- Payment retries and idempotency handling
- Partial and full refunds
- Item level billing

Services mostly communicate over REST where we need a clear response back. For other things, domain events are published to Kafka so we have auditability and flexibility to extend later.

---

## Infrastructure Components

| Component | Purpose |
|---------|--------|
| PostgreSQL | Persistent storage (separate DB for each service) |
| Redis | Cache for menus and frequently used order data |
| Kafka | Domain events, audit logs and async workflows |
| Docker Compose | Local orchestration and deployment simulation |

---

## Event Driven Design

Kafka is **not** used for making transactional decisions, especially around payments. It is only used to publish events after the main operation is completed.

Some example events:
- ORDER_CREATED
- PAYMENT_SUCCEEDED
- PAYMENT_FAILED
- REFUND_ISSUED

This helps with:
- Keeping payments strongly consistent
- Eventual consistency for downstream consumers
- Easier audits and event replay if required

---

## Security Model

Each service applies its own security using Spring Security and JWT based auth.

- Role based access control
- Stateless APIs
- Service to service communication uses internal system authentication

This avoids having one centralized security service and fits better with zero trust kind of setups.

---

## Running the System

### Prerequisites
- Docker
- Docker Compose

### Start the platform
```bash
docker compose up --build
```
### How to get authentication token
```bash
curl -X POST http://localhost:9000/token \
  -H "Content-Type: application/json" \
  -d '{
        "client_id": "<client_id>",
        "client_secret": "<client_secret>"
      }'
```
