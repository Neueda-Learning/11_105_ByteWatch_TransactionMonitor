# Quality, Testing, and Operations

This document captures quality controls, validation strategy, accessibility coverage, and deployment operations.

## Testing Status Snapshot

Latest local backend run:
- Date: 2026-08-06
- Command: mvn test
- Result: PASS
- Total tests: 50
- Failures: 0
- Errors: 0
- Skipped: 0

## Code Coverage Metrics (Backend)

Coverage source:
- Tool: JaCoCo
- Report artifact: backend/target/site/jacoco/jacoco.csv
- Generation command: mvn org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report

Summary percentages:
- Instruction coverage: 95.40% (2718/2849)
- Line coverage: 94.69% (713/753)
- Branch coverage: 69.23% (90/130)
- Method coverage: 96.28% (259/269)
- Complexity coverage: 86.23% (288/334)

How to read this quickly:
- High instruction/line/method coverage indicates broad execution of core code paths.
- Branch coverage is the main improvement area and typically increases with more negative-path and boundary-case tests.

## Test Coverage Scope

Current test suites cover:

- Configuration
  - CORS config behavior
- Controllers (integration-style with MockMvc)
  - AlertController
  - TransactionController
  - SimulationController
- Services (unit)
  - AlertService
  - RuleEngineService
  - TransactionFeedService
  - TransactionSimulationService
- Repository behavior
  - JDBC repository tests
- Models
  - Model coverage tests
- Application bootstrapping
  - Spring Boot application tests

What this gives confidence in:
- API contract behavior and status mappings
- Rule scoring logic and severity classification
- Lifecycle transition validation and audit writes
- Simulation control and transaction feed behavior

## Validation Strategy

Validation is layered so invalid data is stopped early:

### Layer 1: Request model validation
- Transaction payload uses bean validation annotations.
- Invalid fields return HTTP 400 with field-level error structure.

### Layer 2: Service guardrails
- Service methods validate null/incomplete request states.
- IllegalArgumentException is used for consistent client-facing 400 errors.

### Layer 3: Persistence constraints
- Duplicate transaction IDs are rejected and surfaced as HTTP 409.

## Accessibility Coverage (Frontend)

Accessibility features present in the UI:

- Labeled control groups and sections
- aria-live for changing content
- Accessible button semantics and pressed-state hints
- Focus-visible form controls
- Structured headings and meaningful region labels

Known practical note:
- The app is keyboard-usable for primary navigation/actions.

## Portability and Deployment

### Local portability
- Frontend can run directly as static files.
- Backend can run locally with MySQL using environment-driven credentials.

### Container portability
- Docker Compose defines mysql + backend + frontend.
- MySQL healthchecks protect backend startup ordering.
- Frontend is served by Nginx container.

### CI pipeline
Jenkins pipeline performs:
1. Checkout
2. Backend clean verify (tests gate deployment)
3. Preflight checks
4. Stop existing stack
5. Build images
6. Deploy compose stack
7. Health verification

## Reliability Controls Included

- Deterministic schema/data initialization for repeatable demo state
- Structured API error payloads for client reliability
- Simulation status endpoints for observability of generated traffic
- Actuator health endpoint used in pipeline verification

## Recommended Next Quality Additions

- Add JaCoCo coverage reporting with thresholds in CI.
- Add API-level contract tests against running containers.
- Add a lightweight frontend smoke test in pipeline.
- Add a11y automation checks (for example axe-based scans).
