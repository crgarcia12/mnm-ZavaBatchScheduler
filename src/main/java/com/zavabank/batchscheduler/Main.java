package com.zavabank.batchscheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static volatile boolean running = true;

    public static void main(String[] args) {
        final Properties config = loadConfig("batchscheduler.properties");
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final long intervalMs = parseLong(config.getProperty("scheduler.interval.ms", "300000"), 300000L);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                running = false;
                log("Shutdown requested.");
                scheduler.shutdownNow();
            }
        }));

        log("ZavaBatchScheduler started. Interval=" + intervalMs + "ms");
        scheduler.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                if (!running) {
                    return;
                }
                executeBatchRun(config);
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        while (running) {
            sleepQuietly(1000L);
        }
        log("ZavaBatchScheduler stopped.");
    }

    private static void executeBatchRun(Properties config) {
        Date runStart = new Date();
        boolean interestOk = callService(config, "interest.accrual", config.getProperty("interest.service.url"), config.getProperty("interest.service.endpoint"), "{\"job\":\"interest_accrual\"}");
        boolean statementOk = callService(config, "statement.generation", config.getProperty("statement.service.url"), config.getProperty("statement.service.endpoint"), "{\"job\":\"statement_generation\"}");
        boolean reconOk = callService(config, "daily.reconciliation", config.getProperty("reconciliation.service.url"), config.getProperty("reconciliation.service.endpoint"), "{\"job\":\"daily_reconciliation\"}");

        String status = (interestOk && statementOk && reconOk) ? "SUCCESS" : "FAILED";
        String details = "interest=" + interestOk + ", statements=" + statementOk + ", reconciliation=" + reconOk;
        log("Batch run status: " + status + " (" + details + ")");
        insertBatchRunLog(config, runStart, new Date(), status, details);
    }

    private static boolean callService(Properties config, String jobName, String baseUrl, String endpoint, String payload) {
        HttpURLConnection connection = null;
        OutputStream output = null;
        InputStream responseStream = null;
        try {
            if (baseUrl == null || endpoint == null) {
                log("Skipping " + jobName + " due to missing URL/endpoint.");
                return false;
            }
            URL url = new URL(baseUrl + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(parseInt(config.getProperty("http.timeout.ms", "15000"), 15000));
            connection.setReadTimeout(parseInt(config.getProperty("http.timeout.ms", "15000"), 15000));
            connection.setRequestProperty("Content-Type", "application/json");

            output = connection.getOutputStream();
            output.write(payload.getBytes(StandardCharsets.UTF_8));
            output.flush();

            int status = connection.getResponseCode();
            responseStream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            String body = readStream(responseStream);
            log(jobName + " response [" + status + "]: " + body);
            return status >= 200 && status < 300;
        } catch (Exception ex) {
            log(jobName + " failed: " + ex.getMessage());
            return false;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void insertBatchRunLog(Properties config, Date startedAt, Date endedAt, String status, String details) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.username"),
                config.getProperty("db.password"));
            ps = conn.prepareStatement(
                "INSERT INTO BatchRunLog (RunType, StartedAt, EndedAt, Status, Details) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, "nightly");
            ps.setTimestamp(2, new Timestamp(startedAt.getTime()));
            ps.setTimestamp(3, new Timestamp(endedAt.getTime()));
            ps.setString(4, status);
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception ex) {
            log("BatchRunLog insert failed: " + ex.getMessage());
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception ignored) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static Properties loadConfig(String classpathFile) {
        Properties props = new Properties();
        InputStream stream = null;
        try {
            stream = Main.class.getClassLoader().getResourceAsStream(classpathFile);
            if (stream != null) {
                props.load(stream);
            }
        } catch (Exception ex) {
            log("Could not load config file " + classpathFile + ": " + ex.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        applyEnvOverride(props, "BATCH_SCHEDULER_INTERVAL_MS", "scheduler.interval.ms");
        applyEnvOverride(props, "INTEREST_SERVICE_URL", "interest.service.url");
        applyEnvOverride(props, "INTEREST_SERVICE_ENDPOINT", "interest.service.endpoint");
        applyEnvOverride(props, "STATEMENT_SERVICE_URL", "statement.service.url");
        applyEnvOverride(props, "STATEMENT_SERVICE_ENDPOINT", "statement.service.endpoint");
        applyEnvOverride(props, "RECON_SERVICE_URL", "reconciliation.service.url");
        applyEnvOverride(props, "RECON_SERVICE_ENDPOINT", "reconciliation.service.endpoint");
        applyEnvOverride(props, "SQLSERVER_URL", "db.url");
        applyEnvOverride(props, "SQLSERVER_USERNAME", "db.username");
        applyEnvOverride(props, "SQLSERVER_PASSWORD", "db.password");

        return props;
    }

    private static void applyEnvOverride(Properties props, String envName, String key) {
        String value = System.getenv(envName);
        if (value != null && value.trim().length() > 0) {
            props.setProperty(key, value);
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static String nowIso() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    }

    private static void log(String message) {
        System.out.println("[ZavaBatchScheduler][" + nowIso() + "] " + message);
    }
}
