# 11_105_ByteWatch_TransactionMonitor
A real-time transaction monitoring and alerting dashboard backend. Built using Spring Boot and plain JDBC, this service evaluates incoming transactions against configurable rules to generate and manage security alerts.

## Branch Safety Rules
1. Treat `develop` as the integration branch for all backend/frontend work.
2. Merge `develop` into `main` only after `mvn -f backend/pom.xml clean compile` and `mvn -f backend/pom.xml test` pass.
3. Avoid direct commits on `main` except emergency hotfixes.

### Local Push Guard
This repository includes a local Git pre-push guard at `.githooks/pre-push` that blocks direct pushes to `main` by default.

Enable it in your local clone:

```bash
git config core.hooksPath .githooks
```

Emergency override (one-off):

```bash
BYPASS_MAIN_PUSH=1 git push origin main
```
