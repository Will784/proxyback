package com.loghub.config;

import com.loghub.domain.LogDomain;
import com.loghub.domain.LogEntry;
import com.loghub.domain.LogLevel;
import com.loghub.repository.LogEntryRepository;
import com.loghub.support.Hashing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Loads demo rows straight into the repository (bypassing proxies) so the UI has something to show. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private static final int ROWS_PER_DOMAIN = 12;
    private static final LogLevel[] LEVELS = LogLevel.values();

    private final LogEntryRepository repository;

    public DemoDataSeeder(LogEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        List<LogEntry> rows = new ArrayList<>();
        Instant now = Instant.now();
        for (LogDomain domain : LogDomain.values()) {
            for (int i = 0; i < ROWS_PER_DOMAIN; i++) {
                LogLevel level = LEVELS[(i * 7 + domain.ordinal()) % LEVELS.length];
                String source = sourceFor(domain);
                String message = messageFor(domain, i);
                rows.add(new LogEntry(domain, level, source, message, payloadFor(domain, i),
                        "10.20." + (domain.ordinal() + 1) + "." + (10 + i), Hashing.sha256(source, message),
                        now.minus((ROWS_PER_DOMAIN - i) * 7L, ChronoUnit.MINUTES)));
            }
        }
        repository.saveAll(rows);
    }

    private String sourceFor(LogDomain domain) {
        return switch (domain) {
            case SECURITY -> "auth-gateway";
            case INGESTION -> "checkout-service";
            case TRACE -> "order-tracer";
            case ANALYTICS -> "metrics-collector";
        };
    }

    private String messageFor(LogDomain domain, int i) {
        return switch (domain) {
            case SECURITY -> i % 3 == 0 ? "Login failed for user u" + i : "Token refreshed for user u" + i;
            case INGESTION -> "Order batch " + i + " received";
            case TRACE -> "Distributed trace #" + i + " completed";
            case ANALYTICS -> "Hourly aggregate " + i + " computed";
        };
    }

    private String payloadFor(LogDomain domain, int i) {
        if (domain != LogDomain.TRACE) {
            return "{\"seq\":" + i + "}";
        }
        StringBuilder spans = new StringBuilder("{\"traceId\":\"t-" + i + "\",\"spans\":[");
        for (int s = 0; s < 40; s++) {
            spans.append(s > 0 ? "," : "")
                    .append("{\"span\":").append(s).append(",\"service\":\"svc-").append(s % 6)
                    .append("\",\"durationMs\":").append(5 + (s * 13 + i) % 90).append('}');
        }
        return spans.append("]}").toString();
    }
}
