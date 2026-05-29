# Configuration & Externalized Settings Inventory

ZavaBatchScheduler has a minimal configuration landscape: one classpath properties file (`batchscheduler.properties`) and environment-variable overrides cover all runtime settings, with no Spring framework, profiles system, or secrets manager in use.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|--------|------|--------------|-------|
| `batchscheduler.properties` | Java `.properties` file | `src/main/resources/batchscheduler.properties` | Primary config file; loaded from classpath at startup via `ClassLoader.getResourceAsStream()` |
| Environment variables | OS/container env vars | Injected at container runtime | Override any property defined in the properties file; full list in Properties Inventory |
| Dockerfile (build args) | Container build config | `Dockerfile` | Defines base images; no `ARG`/`ENV` lines that set application properties |

No Spring Cloud Config server, Azure App Configuration, AWS AppConfig, Consul KV, HashiCorp Vault, or Azure Key Vault integration is present.

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---------|-----------|---------|--------------------------|
| (default) | Always active — single Gradle build | Compiles Java 8 sources, produces fat-jar | `com.github.johnrengelman.shadow:7.1.2` (uber-jar), `application` plugin (entrypoint) |

No Maven/Gradle multi-profile setup exists. There is one configuration for all environments; environment differentiation is handled entirely at runtime via environment variables.

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---------|-----------------|-------------|--------------|
| (single, unnamed) | Default — no profile switching mechanism | `batchscheduler.properties` | All properties overridable via environment variables at container start |

No Spring Profiles, `application-{profile}.yml`, `.env.{environment}`, or `ASPNETCORE_ENVIRONMENT`-style profile system is used. There is no mechanism for dev/staging/production profile differentiation beyond environment variable injection.

## Properties Inventory

### ZavaBatchScheduler

| Property Key | Default Value | Env-Var Override | Type | Notes |
|-------------|--------------|-----------------|------|-------|
| `scheduler.interval.ms` | `300000` | `BATCH_SCHEDULER_INTERVAL_MS` | long | Delay between batch run cycles in milliseconds (default 5 minutes) |
| `http.timeout.ms` | `15000` | _(none defined)_ | int | Connect and read timeout for all HTTP service calls in milliseconds |
| `interest.service.url` | `http://zavainterestcalculator:8080` | `INTEREST_SERVICE_URL` | String | Base URL for interest accrual service |
| `interest.service.endpoint` | `/api/batch/accrue-interest` | `INTEREST_SERVICE_ENDPOINT` | String | Path appended to base URL for the interest accrual call |
| `statement.service.url` | `http://zavastatementservice:8080` | `STATEMENT_SERVICE_URL` | String | Base URL for statement generation service |
| `statement.service.endpoint` | `/api/batch/generate-statements` | `STATEMENT_SERVICE_ENDPOINT` | String | Path appended to base URL for the statement generation call |
| `reconciliation.service.url` | `http://zavaledger:8080` | `RECON_SERVICE_URL` | String | Base URL for daily reconciliation service |
| `reconciliation.service.endpoint` | `/api/batch/reconcile-daily` | `RECON_SERVICE_ENDPOINT` | String | Path appended to base URL for the reconciliation call |
| `db.url` | `jdbc:sqlserver://sqlserver:1433;databaseName=ZavaBankCore;encrypt=false;trustServerCertificate=true` | `SQLSERVER_URL` | String | Full JDBC connection URL; TLS disabled by default |
| `db.username` | `sa` | `SQLSERVER_USERNAME` | String | SQL Server login name |
| `db.password` | `YourStrong!Passw0rd` | `SQLSERVER_PASSWORD` | String | **Sensitive** — plaintext password; must be replaced by a secret reference |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | CPU | Instance Count |
|---------|-------------------|--------|-----|---------------|
| ZavaBatchScheduler | None explicitly configured; defaults used (`java -jar app.jar`) | Not specified in Dockerfile or Compose | Not specified | 1 (single-instance; no scaling mechanism) |

No `-Xms`/`-Xmx` heap settings, `-D` system properties, or JVM tuning flags are defined anywhere in the project. No Docker Compose `mem_limit`, Kubernetes `resources.requests/limits`, or cloud deployment sizing configuration exists.

## Startup Dependency Chain

```
SQL Server (sqlserver:1433)       — must be reachable before first batch run writes BatchRunLog
  ↑
ZavaBatchScheduler (startup)      — loads config, starts scheduler thread, fires first run at t=0

zavainterestcalculator:8080       — must be reachable at run time (not at startup)
zavastatementservice:8080         — must be reachable at run time (not at startup)
zavaledger:8080                   — must be reachable at run time (not at startup)
```

There is **no startup wait mechanism** (no `dockerize`, no Docker Compose `depends_on` with health checks, no Kubernetes readiness/liveness probes). The application starts immediately and fires the first batch run at `t=0` ms delay. If the database or downstream services are not yet available at that moment, the first run will be logged as `FAILED`.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Stored In | Masked Value |
|-----------------|------|-----------|-------------|
| `db.password` | Database password | `batchscheduler.properties` (plaintext) | `[MASKED]` |
| `db.username` | Database username | `batchscheduler.properties` (plaintext) | `[MASKED]` |
| `db.url` | JDBC connection string (contains host/DB name) | `batchscheduler.properties` (plaintext) | `[MASKED]` |

### Secrets Provisioning Workflow

**Current state (no secrets management):** All sensitive values are hardcoded in `batchscheduler.properties` and committed to the repository. There is no vault, secrets manager, encrypted properties, or identity-based access. The only opt-out mechanism is environment variable injection at container runtime (`SQLSERVER_PASSWORD`, `SQLSERVER_USERNAME`, `SQLSERVER_URL`), but this relies on the orchestration layer to supply the values correctly.

**Recommended Azure workflow:** Migrate all secrets to **Azure Key Vault**. Grant the container's **system-assigned managed identity** `get` and `list` permissions on the Key Vault. At container startup, the application (or a sidecar/init container) retrieves secret values and injects them as environment variables, replacing the plaintext defaults in `batchscheduler.properties`.

## Feature Flags

No feature flag framework is present. There are no `@ConditionalOnProperty`, `@ConditionalOnExpression`, LaunchDarkly, Unleash, or custom toggle implementations. All code paths are unconditionally active.

| Flag Name | Default | Controlled By |
|-----------|---------|--------------|
| _(none detected)_ | — | — |

## Framework & Runtime Versions

| Component | Version | Source |
|-----------|---------|--------|
| Java (source/target compatibility) | 1.8 (Java 8) | `build.gradle` — `sourceCompatibility = JavaVersion.VERSION_1_8` |
| Gradle | 7.6 | `Dockerfile` — `FROM gradle:7.6-jdk8` |
| Shadow (fat-jar) plugin | 7.1.2 | `build.gradle` — `id 'com.github.johnrengelman.shadow' version '7.1.2'` |
| mssql-jdbc (SQL Server driver) | 12.6.1.jre8 | `build.gradle` — `implementation` scope |
| amqp-client (RabbitMQ) | 5.21.0 | `build.gradle` — `implementation` scope |
| Docker build base image | `gradle:7.6-jdk8` | `Dockerfile` — build stage |
| Docker runtime base image | `eclipse-temurin:8-jre` | `Dockerfile` — runtime stage |
