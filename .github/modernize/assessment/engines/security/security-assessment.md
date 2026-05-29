# Security Assessment Report

**Generated:** 2026-05-29T07:44:45Z

## Summary

| Metric | Count |
|--------|-------|
| Total Findings | 4 |
| CVE Vulnerabilities | 0 |
| CWE Vulnerabilities | 4 |
| Total Rules Assessed | 59 |
| Rules Passed | 55 |

### By Severity

| Severity | Count |
|----------|-------|
| mandatory | 0 |
| optional | 3 |
| potential | 1 |

## CVE Findings (Dependency Vulnerabilities)

No CVE vulnerabilities found in the declared dependencies (`com.rabbitmq:amqp-client:5.21.0`, `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre8`).

## CWE Findings (Code-Level Vulnerabilities)

### CWE-477: Use of Obsolete Function
- **Category:** Code Quality
- **Severity:** optional
- **Story Points:** 1
- **Files:** `src/main/java/com/zavabank/batchscheduler/Main.java`

The codebase uses legacy `java.util.Date` (lines 55, 63, 114, 233) and `java.text.SimpleDateFormat` (line 233) — both of which have been superseded since Java 8 by the `java.time.*` API (`LocalDateTime`, `ZonedDateTime`, `DateTimeFormatter`). `SimpleDateFormat` is additionally not thread-safe, creating a latent risk if the instance were shared. The `nowIso()` method (line 233) instantiates a new `SimpleDateFormat` on every call, which is wasteful and relies on an obsolete formatter.

---

### CWE-259: Use of Hard-coded Password
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** `src/main/resources/batchscheduler.properties`

A plaintext SQL Server password is hardcoded at line 15 of `batchscheduler.properties`. This file is packaged into the fat-jar at build time, meaning anyone with access to the jar can extract the credential. Although an environment variable override (`SQLSERVER_PASSWORD`) is supported, the default value remains committed to the repository.

---

### CWE-798: Use of Hard-coded Credentials
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** `src/main/resources/batchscheduler.properties`

Both the SQL Server username and password are hardcoded in `batchscheduler.properties`: `db.username=sa` (line 14) and `db.password` (line 15). The username `sa` is the SQL Server system administrator account — using this account for application connections violates the principle of least privilege. Both credentials are committed to version control and embedded in the application jar, exposing them to anyone with repository or artifact access.

---

### CWE-778: Insufficient Logging
- **Category:** Credentials & Secrets
- **Severity:** potential
- **Story Points:** 3
- **Files:** `src/main/java/com/zavabank/batchscheduler/Main.java`

The application uses raw `System.out.println` for all logging (`log()` method, line 238). Security-critical events such as database connection failures (`insertBatchRunLog` catch block, ~line 135) and HTTP service call failures (`callService` catch block, ~line 93) are logged only as plain text messages with minimal context — no timestamp correlation with the batch run ID, no severity level, no structured fields. There is no logging framework (SLF4J, Log4j, Logback) configured, making it impossible to route security-relevant events to a SIEM or centralized log aggregation system.
