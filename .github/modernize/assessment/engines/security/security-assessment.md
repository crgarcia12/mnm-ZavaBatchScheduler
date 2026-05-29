# Security Assessment Report

**Generated:** 2026-05-29T03:54:18.0000000Z

## Summary

| Metric | Count |
|--------|-------|
| Total Findings | 2 |
| CVE Vulnerabilities | 0 |
| CWE Vulnerabilities | 2 |
| Total Rules Assessed | 59 |
| Rules Passed | 57 |

### By Severity

| Severity | Count |
|----------|-------|
| mandatory | 0 |
| optional | 2 |
| potential | 0 |

## CVE Findings (Dependency Vulnerabilities)

No CVE findings met the high severity threshold.

## CWE Findings (Code-Level Vulnerabilities)

### CWE-259: Use of Hard-coded Password
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** src/main/resources/batchscheduler.properties:15

A static database password value (`db.****** is defined in src/main/resources/batchscheduler.properties:15 and consumed for outbound SQL Server authentication in Main.insertBatchRunLog via config property lookup.

### CWE-798: Use of Hard-coded Credentials
- **Category:** Credentials & Secrets
- **Severity:** optional
- **Story Points:** 5
- **Files:** src/main/resources/batchscheduler.properties:14, src/main/resources/batchscheduler.properties:15

The configuration file stores static DB credentials (`db.username=sa`, `db.****** that are read at runtime for SQL Server connection setup.
