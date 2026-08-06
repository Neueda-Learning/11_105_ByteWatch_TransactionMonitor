# ByteWatch Transaction Monitor

ByteWatch helps analysts spot risky payment behavior early by monitoring incoming transactions, scoring risk signals, and creating actionable alerts.

This repository includes:
- A backend that evaluates transactions against fraud-like rules and records the alert lifecycle.
- A portable, browser-based UI for triage, live simulation, and transaction visibility.
- Automation and test coverage to support reliable demos and releases.


## At A Glance

### Problem it solves
Manual alert handling is slow and inconsistent when transaction volume grows. ByteWatch automates first-pass risk detection and gives analysts a structured review flow.

### How it works
1. A transaction enters the system.
2. The rule engine evaluates four risk checks.
3. If risk rules trigger, an alert is opened with a severity score.
4. Analysts review, investigate, and close or dismiss using a controlled lifecycle.
5. Every action is logged for traceability.

### Core outcomes
- Faster identification of suspicious activity.
- Consistent alert handling process.
- Auditability of analyst actions.
- A demo-ready workflow that can run locally or in containers.

## Functional Highlights

- Transaction intake with validation and duplicate-protection.
- Multi-rule risk scoring with severity tiers.
- Alert queue, detail view, lifecycle actions, and audit trail.
- Live simulation feed to generate realistic transaction traffic.
- Portable UI that runs as a static app and adapts to desktop/mobile.
- Demo fallback mode in UI when backend is unreachable.

## Extra Enhancements Included

- Portable UI behavior:
  - Responsive layout for desktop and mobile.
  - Stateless static frontend packaging with Docker + Nginx.
  - No build-step dependency for basic browser use.
- Accessibility improvements:
  - Semantic regions and labels in the UI.
  - Live region usage for dynamic updates.
  - Keyboard-friendly controls and visible focus states.
- Data validation:
  - Request-level validation for transaction payloads.
  - Service-level guardrails for null/incomplete requests.
  - Structured API error responses for clients.
- Quality and automation:
  - Unit and integration tests across controllers, services, repository behavior, models, and configuration.
  - CI pipeline runs backend build and tests before container deployment.

## Detailed Documentation Map

- Backend implementation details: [docs/IMPLEMENTATION-BACKEND.md](docs/IMPLEMENTATION-BACKEND.md)
- Frontend and UX details: [docs/IMPLEMENTATION-FRONTEND.md](docs/IMPLEMENTATION-FRONTEND.md)
- Testing, coverage, accessibility, and operations: [docs/QUALITY-TESTING-OPERATIONS.md](docs/QUALITY-TESTING-OPERATIONS.md)

## Quick Start

### Option 1: Docker Compose (recommended for full stack)
1. Set DB password in environment (or .env for compose).
2. Run from repository root:

```bash
docker-compose up --build
```

3. Open:
- Frontend: http://localhost:8081
- Backend health: http://localhost:8082/actuator/health

### Option 2: Local backend + static frontend
1. Start MySQL and set DB credentials for backend runtime.
2. Run backend from [backend](backend):

```bash
mvn spring-boot:run
```

3. Open [frontend/index.html](frontend/index.html) directly in browser.

## Current Validation Snapshot

Latest local backend run:
- Command: mvn test
- Result: PASS
- Total tests: 50
- Failures: 0
- Errors: 0
- Skipped: 0

## Code Coverage Snapshot (Backend)

Latest JaCoCo summary:
- Instruction coverage: 95.40% (2718/2849)
- Line coverage: 94.69% (713/753)
- Branch coverage: 69.23% (90/130)
- Method coverage: 96.28% (259/269)
- Complexity coverage: 86.23% (288/334)

Interpretation at a glance:
- Overall execution-path coverage is strong (instruction/line/method).
- Branch coverage is lower than line coverage, indicating room to expand edge-case and conditional-path testing.

## Repository Structure

```text
backend/     Spring Boot APIs, rule engine, persistence, tests
frontend/    Static UI (HTML/CSS/JS), live dashboard and transaction view
docs/        Audience-focused technical documentation
```

## Notes

- This project intentionally uses Spring JDBC for explicit SQL control.
- Seeded schema/data support consistent demo behavior across runs.
