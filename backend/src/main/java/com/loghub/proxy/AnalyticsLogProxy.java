package com.loghub.proxy;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.dto.ServiceStats;
import com.loghub.service.LogService;
import com.loghub.support.LruCache;

import java.time.Duration;
import java.util.Optional;

/** Smart proxy: aggregates are expensive, so they are cached with a TTL and invalidated by every write. */
public final class AnalyticsLogProxy extends AbstractLogProxy {

    private static final String STATS_KEY = "stats";

    private final LruCache<String, ServiceStats> statsCache;

    public AnalyticsLogProxy(LogService delegate, AuditTrail auditTrail, Duration statsTtl) {
        super(LogDomain.ANALYTICS, delegate,
                new ProxyMetrics("calls", "statsHits", "statsMisses", "invalidations"), auditTrail);
        this.statsCache = new LruCache<>(1, statsTtl);
    }

    @Override
    public String proxyKind() {
        return "Proxy inteligente de agregados";
    }

    @Override
    public String responsibility() {
        return "Guarda las estadísticas calculadas unos segundos y las descarta apenas llega un log nuevo.";
    }

    @Override
    protected ServiceStats onStats(AccessContext context) {
        Optional<ServiceStats> cached = statsCache.get(STATS_KEY);
        if (cached.isPresent()) {
            metrics.increment("statsHits");
            return cached.get();
        }
        metrics.increment("statsMisses");
        ServiceStats fresh = delegate.stats(context);
        statsCache.put(STATS_KEY, fresh);
        return fresh;
    }

    @Override
    protected LogResponse onSave(LogRequest request, AccessContext context) {
        LogResponse saved = delegate.save(request, context);
        statsCache.clear();
        metrics.increment("invalidations");
        return saved;
    }
}
