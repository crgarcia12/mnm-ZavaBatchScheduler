# Architecture Diagram

This document summarizes the scheduler application structure and its runtime component relationships.

## Application Architecture

```mermaid
flowchart TD
    subgraph Runtime["Application Runtime - Java 8"]
        Scheduler["Batch Scheduler Loop"]
        HttpClient["HTTP Job Dispatcher"]
        DbWriter["Batch Log Writer"]
    end
    subgraph Data["Data Layer"]
        SqlServer[("SQL Server ZavaBankCore")]
    end
    subgraph External["External Services"]
        InterestSvc["Interest Calculator Service"]
        StatementSvc["Statement Service"]
        ReconSvc["Ledger Reconciliation Service"]
    end

    Scheduler -->|"triggers jobs"| HttpClient
    HttpClient -->|"POST accrue-interest"| InterestSvc
    HttpClient -->|"POST generate-statements"| StatementSvc
    HttpClient -->|"POST reconcile-daily"| ReconSvc
    Scheduler -->|"writes run result"| DbWriter
    DbWriter -->|"INSERT BatchRunLog"| SqlServer
```

### Technology Stack Summary

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Runtime | Java SE | 8 | Executes scheduled batch orchestration |
| Build | Gradle | 7.x plugin set | Builds and packages application |
| Data Access | JDBC + SQL Server driver | mssql-jdbc 12.6.1.jre8 | Persists batch execution logs |
| Integration | HTTP via HttpURLConnection | JDK built-in | Invokes downstream batch APIs |

### Data Storage & External Services

The scheduler stores batch run status in a SQL Server database table (`BatchRunLog`) and calls three external HTTP services for interest accrual, statement generation, and daily reconciliation.

### Key Architectural Decisions

- Uses a single-process scheduler (`ScheduledExecutorService`) for fixed-delay batch execution.
- Uses direct HTTP calls to downstream services with timeout configuration from properties.
- Uses direct JDBC writes for operational run logging.

## Component Relationships

```mermaid
flowchart LR
    subgraph Presentation["Presentation"]
        RuntimeHook["Main Runtime Entry"]
    end
    subgraph Business["Business Logic"]
        BatchRun["executeBatchRun"]
        ServiceCall["callService"]
    end
    subgraph DataAccess["Data Access"]
        RunLogRepo["insertBatchRunLog"]
    end
    subgraph Infra["Infrastructure"]
        ConfigLoader["loadConfig"]
        SchedulerExec["ScheduledExecutorService"]
        Logger["Console Logger"]
    end

    RuntimeHook -->|"loads settings"| ConfigLoader
    RuntimeHook -->|"starts"| SchedulerExec
    SchedulerExec -->|"executes interval"| BatchRun
    BatchRun -->|"dispatches 3 jobs"| ServiceCall
    BatchRun -->|"persists status"| RunLogRepo
    BatchRun -.->|"logs"| Logger
    ServiceCall -.->|"logs result"| Logger
```

### Component Inventory

| Component | Layer | Type | Responsibility |
|---|---|---|---|
| Main runtime entry | Presentation | Bootstrap | Initializes config, scheduler, and shutdown hook |
| ScheduledExecutorService | Infrastructure | Scheduler | Executes periodic batch runs |
| executeBatchRun | Business Logic | Orchestrator | Runs three downstream jobs and aggregates status |
| callService | Business Logic | HTTP client helper | Calls external services and evaluates success/failure |
| insertBatchRunLog | Data Access | JDBC persistence | Inserts execution status into BatchRunLog table |
| loadConfig | Infrastructure | Configuration loader | Reads properties and environment overrides |
| Console logger | Infrastructure | Logging utility | Emits operational events with timestamps |
