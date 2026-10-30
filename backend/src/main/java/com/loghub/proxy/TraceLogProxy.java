package com.loghub.proxy;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.dto.LogResponse;
import com.loghub.service.LogService;
import com.loghub.support.LruCache;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Virtual + caching proxy: lists never touch the heavy payload; the payload is loaded lazily
 * on first read and then served from an LRU cache with TTL.
 */
public final class TraceLogProxy extends AbstractLogProxy {

    private final LruCache<Long, LogResponse> payloadCache;

    public TraceLogProxy(LogService delegate, AuditTrail auditTrail, int cacheCapacity, Duration cacheTtl) {
        super(LogDomain.TRACE, delegate, new ProxyMetrics("calls", "cacheHits", "cacheMisses"), auditTrail);
        this.payloadCache = new LruCache<>(cacheCapacity, cacheTtl);
    }

    @Override
    public String proxyKind() {
        return "Proxy virtual con caché";
    }

    @Override
    public String responsibility() {
        return "Las listas solo muestran resúmenes; la carga pesada se trae bajo demanda y se guarda en una caché LRU.";
    }

    @Override
    protected LogResponse onFindById(Long id, AccessContext context) {
        Optional<LogResponse> cached = payloadCache.get(id);
        if (cached.isPresent()) {
            metrics.increment("cacheHits");
            return cached.get();
        }
        metrics.increment("cacheMisses");
        LogResponse loaded = delegate.findById(id, context);
        payloadCache.put(id, loaded);
        return loaded;
    }

    @Override
    protected Map<String, Long> metricsSnapshot() {
        Map<String, Long> view = new LinkedHashMap<>(super.metricsSnapshot());
        view.put("cachedEntries", (long) payloadCache.size());
        return view;
    }
}
