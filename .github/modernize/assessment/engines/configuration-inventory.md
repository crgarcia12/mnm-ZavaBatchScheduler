# Configuration & Externalized Settings Inventory

ZavaBatchScheduler uses two configuration sources — a classpath properties file and environment-variable overrides — with no profiles, secrets stores, or external config server.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|--------|------|---------------|-------|
| `batchscheduler.properties` | Java Properties file | `src/main/resources/batchscheduler.properties` | Bundled into fat JAR; loaded at startup from classpath |
| Environment variables | OS/container env | Runtime injection | 10 named variables override corresponding property keys via `applyEnvOverride()` |
| Dockerfile / Docker build | Container build | `Dockerfile` | Multi-stage build; no environment variables set at build time |

No Spring Cloud Config, Azure App Configuration, AWS AppConfig, Consul KV, HashiCorp Vault, or Azure Key Vault integration is present.

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---------|-----------|---------|--------------------------|
| Default (no profiles) | Always active | Single build configuration | Shadow plugin 7.1.2 for fat-JAR packaging |

Gradle does not define multiple build types or flavors. The `build.gradle` file has a single configuration; there is no dev/prod build separation.

## Runtime Profiles

No runtime profiles are configured. The application does not use Spring Boot or any profile-aware framework. The same property file is used in all environments; environment-specific values are injected via environment variables at container start.

| Profile | Activation Method | Config Files | Key Overrides |
|---------|-------------------|--------------|---------------|
| (none — single config) | N/A | `batchscheduler.properties` | All properties overridable via environment variables |

## Properties Inventory

### ZavaBatchScheduler

| Property Key | Default Value | Source | Override Environment Variable |
|--------------|---------------|--------|-------------------------------|
| `scheduler.interval.ms` | `300000` (5 min) | `batchscheduler.properties` | `BATCH_SCHEDULER_INTERVAL_MS` |
| `http.timeout.ms` | `15000` (15 s) | `batchscheduler.properties` | — |
| `interest.service.url` | `http://zavainterestcalculator:8080` | `batchscheduler.properties` | `INTEREST_SERVICE_URL` |
| `interest.service.endpoint` | `/api/batch/accrue-interest` | `batchscheduler.properties` | `INTEREST_SERVICE_ENDPOINT` |
| `statement.service.url` | `http://zavastatementservice:8080` | `batchscheduler.properties` | `STATEMENT_SERVICE_URL` |
| `statement.service.endpoint` | `/api/batch/generate-statements` | `batchscheduler.properties` | `STATEMENT_SERVICE_ENDPOINT` |
| `reconciliation.service.url` | `http://zavaledger:8080` | `batchscheduler.properties` | `RECON_SERVICE_URL` |
| `reconciliation.service.endpoint` | `/api/batch/reconcile-daily` | `batchscheduler.properties` | `RECON_SERVICE_ENDPOINT` |
| `db.url` | `jdbc:sqlserver://sqlserver:1433;databaseName=ZavaBankCore;encrypt=false;trustServerCertificate=true` | `batchscheduler.properties` | `SQLSERVER_URL` |
| `db.username` | `sa` | `batchscheduler.properties` | `SQLSERVER_USERNAME` |
| `db.password` | `YourStrong!Passw0rd` (**plain text**) | `batchscheduler.properties` | `SQLSERVER_PASSWORD` |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory Limit | Instance Count |
|---------|--------------------|--------------|-----------------------------|
| ZavaBatchScheduler | Default JVM flags (none explicitly set) | Not specified in Dockerfile | 1 (single-threaded scheduler) |

No `-Xms`/`-Xmx` heap settings, GC flags, or module-system arguments are configured. The Dockerfile `CMD` is simply `java -jar app.jar`. Container memory and CPU limits are not defined in the repository.

## Startup Dependency Chain

The application has no formal startup dependency mechanism. At startup, `loadConfig()` reads `batchscheduler.properties` and applies environment-variable overrides. The scheduler fires its first run immediately (initial delay = 0). If the downstream services or SQL Server are not yet ready, the first batch run will fail and be logged.

| Step | Depends On | Wait Mechanism | Timeout |
|------|-----------|----------------|---------|
| Config load | Classpath resource | Synchronous file read | Immediate |
| First batch run | Downstream services reachable | None — fires immediately | Per-call `http.timeout.ms` (15 s) |
| DB insert | SQL Server reachable | None | JDBC connection timeout (driver default) |

There is no `dockerize`, Kubernetes readiness probe, or `depends_on` health-check configured.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage |
|-----------------|------|---------|
| `db.password` | Database password | Plain text in `batchscheduler.properties` — **[MASKED: YourStrong!...]** |
| `db.username` | Database username | Plain text (`sa`) in `batchscheduler.properties` |
| `db.url` | JDBC connection string | Plain text with `encrypt=false;trustServerCertificate=true` |

### Secrets Provisioning Workflow

**Current state (insecure):** The database password is committed to the repository as plain text in `src/main/resources/batchscheduler.properties`. Any developer, CI agent, or process with repository read access obtains the credential.

**Recommended workflow for Azure deployment:**
1. Remove `db.password` (and `db.username`) from `batchscheduler.properties` and from version-control history.
2. Store the secret in **Azure Key Vault** or use **Azure Managed Identity** with Azure SQL Entra authentication (eliminates password entirely).
3. At deployment time, inject `SQLSERVER_PASSWORD` (and optionally `SQLSERVER_USERNAME`, `SQLSERVER_URL`) as environment-variable secrets via Azure Container Apps Secrets, AKS Kubernetes Secrets, or GitHub Actions / Azure Pipelines secret variables.
4. The existing `applyEnvOverride()` pattern in the application picks up the injected values at startup without code changes.

## Feature Flags

No feature flag framework or `@ConditionalOnProperty`-style toggles are present. The application has a single code path with no conditional functionality.

| Flag Name | Default | Controlled By |
|-----------|---------|---------------|
| (none) | — | — |

## Framework & Runtime Versions

| Component | Version | Source |
|-----------|---------|--------|
| Java source/target compatibility | 1.8 (Java 8) | `build.gradle` (`sourceCompatibility`, `targetCompatibility`) |
| Gradle (Docker build) | 7.6 | `Dockerfile` (`FROM gradle:7.6-jdk8`) |
| Shadow (fat-JAR) plugin | 7.1.2 | `build.gradle` |
| Microsoft JDBC Driver for SQL Server | 12.6.1.jre8 | `build.gradle` |
| RabbitMQ amqp-client | 5.21.0 | `build.gradle` (unused) |
| Docker build image | `gradle:7.6-jdk8` | `Dockerfile` |
| Docker runtime image | `eclipse-temurin:8-jre` | `Dockerfile` |
