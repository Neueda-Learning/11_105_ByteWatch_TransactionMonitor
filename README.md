# ByteWatch Transaction Monitor

ByteWatch is a transaction monitoring and alert management system built for risk analysts.

The project provides:
- a Spring Boot + JDBC backend that evaluates incoming transactions against risk rules,
- a browser-based frontend for alert triage and lifecycle actions,
- seeded demo data for consistent local testing and demos.

## Project Structure

```text
11_105_ByteWatch_TransactionMonitor/
  backend/
    src/main/java/org/ByteWatch/
      controller/
      model/
      repository/
      service/
      config/
      TransactionMonitoringApplication.java
    src/main/resources/
      application.properties
      schema.sql
      data.sql
    src/test/java/org/ByteWatch/
      controller/
      service/
    pom.xml
  frontend/
    index.html
    script.js
    styles.css
  UserInterview.md
```

## Functional Scope

### Backend capabilities
- Accepts transaction intake requests and persists raw transaction records.
- Evaluates each transaction against four risk rules.
- Creates alerts automatically when one or more rules are triggered.
- Supports alert lifecycle transitions with strict workflow validation.
- Enforces mandatory comments for DISMISSED and CLOSED states.
- Captures alert status history in an audit log.
- Exposes dashboard-oriented alert list and alert detail APIs.

### Frontend capabilities
- Shows active alerts with severity and status filtering.
- Displays alert details, triggered rules, payer/payee context, and audit history.
- Supports status transitions through backend APIs.
- Falls back to demo data when backend is unavailable.

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring JDBC
- MySQL 8+
- springdoc OpenAPI UI 2.8.9
- Maven
- JUnit 5 + Mockito + Spring MockMvc

### Frontend
- HTML5
- Vanilla JavaScript
- CSS3

## Business Rules and Scoring

The rule engine evaluates each transaction against the following rules:

1. High Amount Rule
- Trigger when amount is greater than 10,000.
- Weight: 40

2. High Velocity Rule
- Trigger when a payer has more than 3 transactions in the last 5 minutes.
- Weight: 30

3. New Payee Rule
- Trigger when a payer has no prior transaction history with the payee.
- Weight: 15

4. Daily Limit Rule
- Trigger when payer transaction total in the previous 24 hours exceeds 50,000.
- Weight: 25

Severity levels are derived from total score:
- HIGH: score >= 60
- MEDIUM: score >= 30
- LOW: score < 30

## Alert Lifecycle

Allowed transitions:
- OPEN -> ACKNOWLEDGED
- ACKNOWLEDGED -> INVESTIGATING
- INVESTIGATING -> DISMISSED or CLOSED

Validation rules:
- Direct jumps are rejected.
- Terminal actions DISMISSED and CLOSED require a non-blank comment.
- Each status change writes an entry to alert_log.

## Data Model

Primary tables:
- customers: reference account and customer details.
- transactions: raw intake stream used for rule evaluation.
- alerts: generated risk alerts (status, severity, triggered rules).
- alert_log: audit trail of analyst actions and status changes.

Initialization behavior:
- schema.sql creates tables and performance indexes.
- data.sql resets and reseeds demo data on startup.
- spring.sql.init.mode=always ensures repeatable local demos.

## API Endpoints

Base URL: http://localhost:8080

### Transaction intake
- POST /api/transactions
- Persists transaction and returns intake result including generated alert (if any).

Example payload:

```json
{
  "txnId": "TXN-2001",
  "timestamp": "2026-08-04T10:00:00",
  "amount": 15000.00,
  "currency": "USD",
  "payeeAccNum": "ACC-1003",
  "payerAccNum": "ACC-1010",
  "status": "PENDING",
  "type": "TRANSFER"
}
```

### Active alert queue
- GET /api/alerts
- Returns OPEN, ACKNOWLEDGED, and INVESTIGATING alerts enriched with transaction and customer details.

### Alert detail
- GET /api/alerts/{id}
- Returns complete alert detail including severity level and audit trail.

### Update alert status
- PUT /api/alerts/{id}/status
- Validates lifecycle transition and writes audit log.

Example payload:

```json
{
  "status": "DISMISSED",
  "comment": "Verified legitimate transfer with customer"
}
```

## Local Setup

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8+

### 1) Clone and enter backend

```bash
git clone <repo-url>
cd 11_105_ByteWatch_TransactionMonitor/backend
```

### 2) Configure database credentials

The backend reads credentials from environment variables.

Windows PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
```

macOS/Linux:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

### 3) Run backend

```bash
mvn spring-boot:run
```

If port 8080 is already in use:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### 4) Open API docs
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

## Frontend Usage

The frontend is static and does not require a build step.

Option 1:
- Open frontend/index.html in a browser.

Option 2:
- Serve frontend via any static server and open in browser.

The page calls:
- http://localhost:8080/api/alerts

If backend is unreachable, the UI switches to built-in demo alerts.

## Testing

From backend directory:

Run all tests:

```bash
mvn test
```

Compile only:

```bash
mvn -DskipTests clean compile
```

Current test coverage areas:
- Controller contract tests for transaction intake and alert status update endpoints.
- Service unit tests for lifecycle validation and rule evaluation scoring.

## Troubleshooting

### Cannot find symbol AlertService or service package errors
- Cause: branch drift where service/repository files are missing in current branch.
- Fix: sync from develop and re-run compile.

### Public Key Retrieval is not allowed
- Ensure MySQL connection includes allowPublicKeyRetrieval=true.

### Access denied for DB user
- Verify DB_USERNAME and DB_PASSWORD are set in the same shell session used to start Maven.

### Port already in use
- Start on another port using spring-boot.run.arguments.

### Swagger loads but endpoints fail
- Check backend logs for datasource initialization failures or SQL syntax errors.

## Branching and Release Flow

Recommended workflow:
1. Do regular development on develop.
2. Run compile and tests on develop.
3. Merge develop into main only when build is green.
4. Keep main stable for demos and release snapshots.

For this repository, local push guard hooks may block direct pushes to main unless explicitly overridden.

## Notes

- This project intentionally uses Spring JDBC (not JPA/Hibernate) for explicit SQL control.
- Seed data is deterministic so demos and debugging start from a known state.
