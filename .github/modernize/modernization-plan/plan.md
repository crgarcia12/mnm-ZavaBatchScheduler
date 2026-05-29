# Modernization Plan: modernization-plan

**Project**: ZavaBatchScheduler

---

## Technical Framework

- **Language**: Java 8
- **Framework**: N/A (standalone Java application)
- **Build Tool**: Gradle
- **Database**: Microsoft SQL Server (JDBC)
- **Key Dependencies**: RabbitMQ AMQP client, Microsoft SQL Server JDBC driver

---

## Overview

> This migration modernizes ZavaBatchScheduler for Azure deployment and operations.
> The application currently runs as a standalone Java batch scheduler with
> external service calls and SQL Server logging. The new architecture will:
>
> - Package and deploy the scheduler to Azure Container Apps
> - Improve release reliability with a deployment-focused modernization path
> - Address dependency security risks before cloud deployment
>
> The migration follows a phased approach: security remediation first,
> then deployment modernization.

---

## Migration Impact Summary

| Application | Original Service | New Azure Service | Authentication | Comments |
|-------------|------------------|-------------------|----------------|----------|
| ZavaBatchScheduler | Local/runtime-hosted job process | Azure Container Apps | Managed Identity | Base Azure modernization deployment plan |

---

## Security Compliance

**Description**: Scan all project dependencies for known CVEs and remediate any identified vulnerabilities to ensure the application is secure before deployment.

**Requirements**:
Upgrade vulnerable dependencies to the minimum patched version. If a CVE fix requires a major version upgrade, document the affected dependency, the current version, the upgraded major version, and the breaking change risk. Verify that the project builds and all tests pass after remediation.

**Environment Configuration**:
Runtime environment established by previous tasks (e.g., Java Home, .NET runtime).
Build tool established by previous tasks (e.g., Maven/Gradle, dotnet).

**App Scope**:
- /tmp/workspace/crgarcia12/mnm-ZavaBatchScheduler
