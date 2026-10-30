package com.loghub.config;

import com.loghub.proxy.AbstractLogProxy;
import com.loghub.proxy.AnalyticsLogProxy;
import com.loghub.proxy.AuditTrail;
import com.loghub.proxy.IngestionLogProxy;
import com.loghub.proxy.LogServiceRegistry;
import com.loghub.proxy.LogValidator;
import com.loghub.proxy.SecurityLogProxy;
import com.loghub.proxy.TraceLogProxy;
import com.loghub.service.AnalyticsLogService;
import com.loghub.service.IngestionLogService;
import com.loghub.service.SecurityLogService;
import com.loghub.service.TraceLogService;
import com.loghub.support.TokenBucketRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/** Composition root: wires every RealSubject inside its Proxy. */
@Configuration
public class ProxyConfig {

    private static final int AUDIT_CAPACITY = 200;

    @Bean
    public AuditTrail auditTrail() {
        return new AuditTrail(AUDIT_CAPACITY);
    }

    @Bean
    public LogServiceRegistry logServiceRegistry(
            SecurityLogService security,
            IngestionLogService ingestion,
            TraceLogService trace,
            AnalyticsLogService analytics,
            AuditTrail auditTrail,
            @Value("${app.proxy.ingestion.capacity}") int bucketCapacity,
            @Value("${app.proxy.ingestion.refill-per-second}") double refillPerSecond,
            @Value("${app.proxy.ingestion.duplicate-window-seconds}") long duplicateWindowSeconds,
            @Value("${app.proxy.trace.cache-capacity}") int cacheCapacity,
            @Value("${app.proxy.trace.cache-ttl-seconds}") long cacheTtlSeconds,
            @Value("${app.proxy.analytics.stats-ttl-seconds}") long statsTtlSeconds) {

        List<AbstractLogProxy> proxies = List.of(
                new SecurityLogProxy(security, auditTrail),
                new IngestionLogProxy(ingestion, auditTrail,
                        new TokenBucketRateLimiter(bucketCapacity, refillPerSecond),
                        new LogValidator(), Duration.ofSeconds(duplicateWindowSeconds)),
                new TraceLogProxy(trace, auditTrail, cacheCapacity, Duration.ofSeconds(cacheTtlSeconds)),
                new AnalyticsLogProxy(analytics, auditTrail, Duration.ofSeconds(statsTtlSeconds)));
        return new LogServiceRegistry(proxies);
    }
}
