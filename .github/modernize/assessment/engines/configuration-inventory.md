# Configuration & Externalized Settings Inventory

The application uses a small set of local property files plus environment variable overrides for runtime externalization.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|---|---|---|---|
| batchscheduler.properties | Application properties | src/main/resources/batchscheduler.properties | Default scheduler, downstream URLs, and DB settings |
| Environment variables | Runtime overrides | Process environment | Overrides interval, service endpoints, and DB credentials |
| Dockerfile | Runtime image config | Dockerfile | Packages shadow JAR and defines container entrypoint |

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---|---|---|---|
| default Gradle build | `gradle build` | Compile Java and package executable JAR | `java`, `application`, `shadow` plugins |

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---|---|---|---|
| default | none required | batchscheduler.properties | scheduler interval, HTTP timeout, service URLs, DB credentials |
| env override mode | environment variables | batchscheduler.properties + env | `BATCH_SCHEDULER_INTERVAL_MS`, `*_SERVICE_URL`, `SQLSERVER_*` |

## Properties Inventory

| Property Key | Default | Profiles | Source |
|---|---|---|---|
| scheduler.interval.ms | 300000 | default/env override | file/env |
| http.timeout.ms | 15000 | default/env override | file |
| interest.service.url | http://zavainterestcalculator:8080 | default/env override | file/env |
| interest.service.endpoint | /api/batch/accrue-interest | default/env override | file/env |
| statement.service.url | http://zavastatementservice:8080 | default/env override | file/env |
| statement.service.endpoint | /api/batch/generate-statements | default/env override | file/env |
| reconciliation.service.url | http://zavaledger:8080 | default/env override | file/env |
| reconciliation.service.endpoint | /api/batch/reconcile-daily | default/env override | file/env |
| db.url | jdbc:sqlserver://sqlserver:1433;databaseName=ZavaBankCore;encrypt=false;trustServerCertificate=true | default/env override | file/env |
| db.username | sa | default/env override | file/env |
| db.password | [MASKED] | default/env override | file/env |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | Instance Count |
|---|---|---|---|
| zava-batch-scheduler | none explicitly configured in repository | Not specified | 1 (expected scheduler process) |

## Startup Dependency Chain

1. zava-batch-scheduler starts and loads configuration.
2. Scheduler begins immediately and depends on downstream services (interest, statement, reconciliation) being reachable.
3. Scheduler also depends on SQL Server reachability for logging run status.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage (masked) |
|---|---|---|
| db.password / SQLSERVER_PASSWORD | Database password | property file default + env override |
| db.username / SQLSERVER_USERNAME | Database username | property file default + env override |

### Secrets Provisioning Workflow

Secrets are expected from environment variables at runtime and override file defaults when present. No external secret manager integration is configured.

## Feature Flags

| Flag Name | Default | Controlled By |
|---|---|---|
| None detected | N/A | N/A |

## Framework & Runtime Versions

| Component | Version | Source |
|---|---|---|
| Java target | 1.8 | build.gradle |
| Gradle Shadow plugin | 7.1.2 | build.gradle |
| RabbitMQ AMQP client | 5.21.0 | build.gradle |
| SQL Server JDBC driver | 12.6.1.jre8 | build.gradle |
| Build image | gradle:7.6-jdk8 | Dockerfile |
| Runtime image | eclipse-temurin:8-jre | Dockerfile |
