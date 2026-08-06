# Backend Implementation Guide

This guide describes how the backend is structured and how key behaviors are implemented.

## Stack

- Java 17
- Spring Boot 3.5.5
- Spring Web + Spring Validation
- Spring JDBC (no ORM)
- MySQL 8+
- Maven

## Architecture Overview

The backend follows a layered style:

- Controller layer: HTTP contracts, request/response mapping, exception-to-status translation.
- Service layer: business rules, lifecycle validation, orchestration.
- Repository layer: SQL access through JdbcTemplate-based implementations.
- Model/DTO layer: transport models and view projections.

Main package root:
- backend/src/main/java/org/ByteWatch

## Main Business Flows

### 1. Transaction intake

Endpoint: POST /api/transactions

Flow:
1. Request payload is validated (bean validation + service guardrails).
2. Transaction is persisted.
3. Rule engine evaluates all four rules.
4. Alert is created when one or more rules trigger.
5. Response returns intake result and optional alert.

Implementation touchpoints:
- TransactionController
- AlertService.processTransaction
- RuleEngineService.evaluate

### 2. Alert review lifecycle

Endpoints:
- GET /api/alerts
- GET /api/alerts/{id}
- PUT /api/alerts/{id}/status

Lifecycle policy (strict forward transitions):
- OPEN -> ACKNOWLEDGED
- ACKNOWLEDGED -> INVESTIGATING or DISMISSED
- INVESTIGATING -> DISMISSED or CLOSED

Every status update writes to alert_log for auditability.

Implementation touchpoints:
- AlertController
- AlertService.updateAlertStatus
- AlertRepository + AlertLogRepository

### 3. Live simulation

Endpoints:
- GET /api/simulation/status
- POST /api/simulation/start
- POST /api/simulation/stop

The simulation emits transactions using the same intake pipeline as real API traffic, which keeps behavior realistic and testable.

Implementation touchpoint:
- TransactionSimulationService

## Rule Engine Details

Implemented in RuleEngineService:

- Rule 1: High amount
- Rule 2: High velocity (payer activity in recent window)
- Rule 3: New payee relationship
- Rule 4: Daily limit by payer and currency

Weights:
- Rule 1: 40
- Rule 2: 30
- Rule 3: 15
- Rule 4: 25

Severity mapping:
- HIGH: score >= 60
- MEDIUM: score >= 30 and < 60
- LOW: score < 30

Currency-aware thresholds are used for major currencies (USD, GBP, EUR, INR).

## Validation and Error Handling

### Request validation

Transaction payload validation is enforced via annotations and @Valid:
- Required fields: txnId, timestamp, amount, currency, payer/payee account numbers
- amount must be positive
- currency constrained to allowed values

### Service-level guardrails

Service methods reject null/incomplete payloads with IllegalArgumentException for consistent 400 behavior.

### Exception mapping

Controllers map business/validation exceptions to explicit HTTP responses:
- 400 Bad Request for invalid payload/transition
- 404 Not Found for missing alerts
- 409 Conflict for duplicate transaction keys

## Persistence and Schema

Tables:
- customers
- transactions
- alerts
- alert_log

Initialization:
- schema.sql and data.sql are executed on startup (spring.sql.init.mode=always)
- Seed data supports deterministic local demos and consistent testing

## API Contract Summary

Base paths:
- /api/transactions
- /api/alerts
- /api/simulation

Docs endpoint:
- /swagger-ui/index.html
- /v3/api-docs

## Configuration Notes

Datasource config is environment-driven:
- DB_USERNAME
- DB_PASSWORD

Defaults and behavior are in backend/src/main/resources/application.properties.

## Build and Run

From backend directory:

```bash
mvn clean verify
mvn spring-boot:run
```

## Why Spring JDBC Here

Spring JDBC keeps SQL explicit and easy to inspect, which is useful when rule-related query behavior and joins are core to correctness and auditability.
