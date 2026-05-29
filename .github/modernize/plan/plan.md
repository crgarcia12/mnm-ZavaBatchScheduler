# Modernization Plan: ZavaBatchScheduler Azure Modernization

**Project**: ZavaBatchScheduler

---

## Technical Framework

- **Language**: Java 8
- **Framework**: Java SE scheduled worker (no web framework)
- **Build Tool**: Gradle
- **Database**: SQL Server
- **Key Dependencies**: RabbitMQ client, Microsoft SQL Server JDBC driver

---

## Overview

> This migration modernizes ZavaBatchScheduler and prepares it for Azure.
> The application currently runs as a Java 8 scheduled process with
> direct SQL credentials and non-cloud-native operational patterns.
> The new architecture will:
>
> - Upgrade the runtime and project stack to the latest supported baseline
> - Adopt Azure cloud-native services for identity and secret management
> - Deploy the workload to Azure for managed operations and scalability
>
> The migration follows a phased approach of upgrade, service modernization,
> security remediation, and Azure deployment.

---

## Migration Impact Summary

| Application | Original Service | New Azure Service | Authentication | Comments |
|-------------|------------------|-------------------|----------------|----------|
| ZavaBatchScheduler | Local Java 8 runtime | Modern Java runtime on Azure | Managed Identity | Framework/runtime modernization |
| ZavaBatchScheduler | SQL Server auth with credentials | Azure SQL + MI auth | Managed Identity | Move to cloud-native data access |
| ZavaBatchScheduler | Env/plaintext secrets | Azure Key Vault | Managed Identity | Centralized secret management |
| ZavaBatchScheduler | Self-managed execution | Azure Container Apps | Managed Identity | Deploy app to Azure |
