# ZavaBatchScheduler

## Summary

| Metric | Value |
|--------|-------|
| Total Issues | 14 |
| Mandatory Blockers | 4 |
| Potential Issues | 9 |

## Component Information

| Property | Value |
|----------|-------|
| Language | Java |
| Frameworks | Java SE |
| Build tools | Gradle |
| JDK version | 1.8 |
| Description | Scheduled batch processor that triggers nightly financial operations (interest accrual, statement generation, daily reconciliation) against downstream microservices and logs run status to SQL Server. |
| Application type | Background Service |
| Lines of code | 239 |

## Cloud Readiness Issues

| Issue Name | Criticality | Story Points | Occurrences |
|------------|-------------|--------------|-------------|
| No HTTP health check endpoint exposed — required for container orchestration liveness and readiness probes | Mandatory | 3 | [1](#No_HTTP_health_check_endpoint_exposed_required_for_container_orchestration_liveness_and_readiness_probes) |
| Database credentials stored as plain text in batchscheduler.properties — use Azure Key Vault or managed identity | Mandatory | 3 | [1](#Database_credentials_stored_as_plain_text_in_batchscheduler_properties_use_Azure_Key_Vault_or_managed_identity) |
| Service endpoint URLs contain hard-coded hostnames that assume Docker network naming — use Azure service discovery or environment variables | Potential | 2 | [3](#Service_endpoint_URLs_contain_hard-coded_hostnames_that_assume_Docker_network_naming_use_Azure_service_discovery_or_environment_variables) |
| HTTP (not HTTPS) used for calls to downstream services — all inter-service traffic is unencrypted | Potential | 2 | [3](#HTTP_not_HTTPS_used_for_calls_to_downstream_services_all_inter-service_traffic_is_unencrypted) |
| Usage of java.net.HttpURLConnection — replace with java.net.http.HttpClient (Java 11+) | Potential | 3 | [2](#Usage_of_java_net_HttpURLConnection_replace_with_java_net_http_HttpClient_Java_11) |
| Application logs using System.out.println — no structured logging framework configured | Potential | 2 | [1](#Application_logs_using_System_out_println_no_structured_logging_framework_configured) |
| No metrics or distributed tracing instrumentation — required for Azure Monitor integration | Potential | 3 | [1](#No_metrics_or_distributed_tracing_instrumentation_required_for_Azure_Monitor_integration) |

### Issue Details

<details id="No_HTTP_health_check_endpoint_exposed_required_for_container_orchestration_liveness_and_readiness_probes">
<summary><b>No HTTP health check endpoint exposed — required for container orchestration liveness and readiness probes</b> — affected files</summary>

- `src/main/java/com/zavabank/batchscheduler/Main.java (line 1)`

</details>

<details id="Database_credentials_stored_as_plain_text_in_batchscheduler_properties_use_Azure_Key_Vault_or_managed_identity">
<summary><b>Database credentials stored as plain text in batchscheduler.properties — use Azure Key Vault or managed identity</b> — affected files</summary>

- `src/main/resources/batchscheduler.properties (line 16)`

</details>

<details id="Service_endpoint_URLs_contain_hard-coded_hostnames_that_assume_Docker_network_naming_use_Azure_service_discovery_or_environment_variables">
<summary><b>Service endpoint URLs contain hard-coded hostnames that assume Docker network naming — use Azure service discovery or environment variables</b> — affected files</summary>

- `src/main/resources/batchscheduler.properties (line 5)`
- `src/main/resources/batchscheduler.properties (line 8)`
- `src/main/resources/batchscheduler.properties (line 11)`

</details>

<details id="HTTP_not_HTTPS_used_for_calls_to_downstream_services_all_inter-service_traffic_is_unencrypted">
<summary><b>HTTP (not HTTPS) used for calls to downstream services — all inter-service traffic is unencrypted</b> — affected files</summary>

- `src/main/resources/batchscheduler.properties (line 5)`
- `src/main/resources/batchscheduler.properties (line 8)`
- `src/main/resources/batchscheduler.properties (line 11)`

</details>

<details id="Usage_of_java_net_HttpURLConnection_replace_with_java_net_http_HttpClient_Java_11">
<summary><b>Usage of java.net.HttpURLConnection — replace with java.net.http.HttpClient (Java 11+)</b> — affected files</summary>

- `src/main/java/com/zavabank/batchscheduler/Main.java (line 60)`
- `src/main/java/com/zavabank/batchscheduler/Main.java (line 62)`

</details>

<details id="Application_logs_using_System_out_println_no_structured_logging_framework_configured">
<summary><b>Application logs using System.out.println — no structured logging framework configured</b> — affected files</summary>

- `src/main/java/com/zavabank/batchscheduler/Main.java (line 228)`

</details>

<details id="No_metrics_or_distributed_tracing_instrumentation_required_for_Azure_Monitor_integration">
<summary><b>No metrics or distributed tracing instrumentation — required for Azure Monitor integration</b> — affected files</summary>

- `src/main/java/com/zavabank/batchscheduler/Main.java (line 1)`

</details>

## Upgrade Issues

| Issue Name | Criticality | Story Points | Occurrences |
|------------|-------------|--------------|-------------|
| Application targets Java 8, which is end-of-life for free Oracle builds and below the Java 17 LTS baseline required for modern Azure Java workloads | Mandatory | 5 | [1](#Application_targets_Java_8_which_is_end-of-life_for_free_Oracle_builds_and_below_the_Java_17_LTS_baseline_required_for_modern_Azure_Java_workloads) |
| Usage of java.net.HttpURLConnection — replace with java.net.http.HttpClient (Java 11+) | Potential | 3 | [2](#Usage_of_java_net_HttpURLConnection_replace_with_java_net_http_HttpClient_Java_11) |
| Shadow plugin 7.1.2 is incompatible with Gradle 8+ — upgrade to Shadow 8.x before upgrading Gradle | Potential | 1 | [1](#Shadow_plugin_7_1_2_is_incompatible_with_Gradle_8_upgrade_to_Shadow_8_x_before_upgrading_Gradle) |
| No Gradle wrapper (gradlew) committed — builds are tied to the system Gradle version, causing reproducibility issues | Potential | 1 | [1](#No_Gradle_wrapper_gradlew_committed_builds_are_tied_to_the_system_Gradle_version_causing_reproducibility_issues) |
| Declared dependency com.rabbitmq:amqp-client is not used in any source file | Optional | 1 | [1](#Declared_dependency_com_rabbitmq_amqp-client_is_not_used_in_any_source_file) |

### Issue Details

<details id="Application_targets_Java_8_which_is_end-of-life_for_free_Oracle_builds_and_below_the_Java_17_LTS_baseline_required_for_modern_Azure_Java_workloads">
<summary><b>Application targets Java 8, which is end-of-life for free Oracle builds and below the Java 17 LTS baseline required for modern Azure Java workloads</b> — affected files</summary>

- `build.gradle (line 16)`

</details>

<details id="Usage_of_java_net_HttpURLConnection_replace_with_java_net_http_HttpClient_Java_11">
<summary><b>Usage of java.net.HttpURLConnection — replace with java.net.http.HttpClient (Java 11+)</b> — affected files</summary>

- `src/main/java/com/zavabank/batchscheduler/Main.java (line 60)`
- `src/main/java/com/zavabank/batchscheduler/Main.java (line 62)`

</details>

<details id="Shadow_plugin_7_1_2_is_incompatible_with_Gradle_8_upgrade_to_Shadow_8_x_before_upgrading_Gradle">
<summary><b>Shadow plugin 7.1.2 is incompatible with Gradle 8+ — upgrade to Shadow 8.x before upgrading Gradle</b> — affected files</summary>

- `build.gradle (line 4)`

</details>

<details id="No_Gradle_wrapper_gradlew_committed_builds_are_tied_to_the_system_Gradle_version_causing_reproducibility_issues">
<summary><b>No Gradle wrapper (gradlew) committed — builds are tied to the system Gradle version, causing reproducibility issues</b> — affected files</summary>

- `build.gradle (line 1)`

</details>

<details id="Declared_dependency_com_rabbitmq_amqp-client_is_not_used_in_any_source_file">
<summary><b>Declared dependency com.rabbitmq:amqp-client is not used in any source file</b> — affected files</summary>

- `build.gradle (line 13)`

</details>

## Security Issues

> **Note:** These issues were generated by AI and may contain inaccuracies or incomplete information. Please review carefully.

| Issue Name | Criticality | Story Points | Files |
|------------|-------------|--------------|-------|
|  | Mandatory | 3 | 0 |
|  | Potential | 2 | 0 |
|  | Potential | 2 | 0 |

### Security Issue Details

---

## Codebase Insights

> **Note:** These documents are generated by AI and may contain inaccuracies or incomplete information. Please review carefully.

1. **[Architecture Diagram](facts/architecture-diagram.md)** — Understand the big picture: system layers and component relationships
2. **[Dependency Map](facts/dependency-map.md)** — Know what the project depends on and where the risks are
3. **[API & Service Contracts](facts/api-service-contracts.md)** — See how services communicate and what contracts they expose
4. **[Data Architecture](facts/data-architecture.md)** — Explore data models, storage, and data flow patterns
5. **[Configuration Inventory](facts/configuration-inventory.md)** — Review how the application is configured across environments
6. **[Business Workflows](facts/business-workflows.md)** — Trace end-to-end business processes and domain logic

[Share feedback](https://aka.ms/ghcp-appmod/feedback)
