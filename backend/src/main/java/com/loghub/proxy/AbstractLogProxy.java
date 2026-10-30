package com.loghub.proxy;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.domain.Verdict;
import com.loghub.dto.AuditEvent;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.dto.LogSummary;
import com.loghub.dto.ProxyOverview;
import com.loghub.dto.ServiceStats;
import com.loghub.service.LogService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Proxy base: implements the same Subject interface as the real service and holds a reference to it.
 * Public operations are final (Template Method): they count the call, then run an overridable hook.
 * Concrete proxies override only the hooks where their specialty applies; everything else is plain delegation.
 */
public abstract class AbstractLogProxy implements LogService {

    protected final LogDomain domain;
    protected final LogService delegate;
    protected final ProxyMetrics metrics;
    private final AuditTrail auditTrail;

    protected AbstractLogProxy(LogDomain domain, LogService delegate, ProxyMetrics metrics, AuditTrail auditTrail) {
        this.domain = domain;
        this.delegate = delegate;
        this.metrics = metrics;
        this.auditTrail = auditTrail;
    }

    public abstract String proxyKind();

    public abstract String responsibility();

    public final LogDomain domain() {
        return domain;
    }

    public ProxyOverview overview() {
        return new ProxyOverview(domain, getClass().getSimpleName(), proxyKind(), responsibility(), metricsSnapshot());
    }

    protected Map<String, Long> metricsSnapshot() {
        return metrics.snapshot();
    }

    @Override
    public final LogResponse save(LogRequest request, AccessContext context) {
        metrics.increment("calls");
        return onSave(request, context);
    }

    @Override
    public final List<LogSummary> findRecent(int limit, AccessContext context) {
        metrics.increment("calls");
        return onFindRecent(limit, context);
    }

    @Override
    public final LogResponse findById(Long id, AccessContext context) {
        metrics.increment("calls");
        return onFindById(id, context);
    }

    @Override
    public final ServiceStats stats(AccessContext context) {
        metrics.increment("calls");
        return onStats(context);
    }

    protected LogResponse onSave(LogRequest request, AccessContext context) {
        return delegate.save(request, context);
    }

    protected List<LogSummary> onFindRecent(int limit, AccessContext context) {
        return delegate.findRecent(limit, context);
    }

    protected LogResponse onFindById(Long id, AccessContext context) {
        return delegate.findById(id, context);
    }

    protected ServiceStats onStats(AccessContext context) {
        return delegate.stats(context);
    }

    protected void audit(AccessContext context, String action, Verdict verdict, String detail) {
        auditTrail.record(new AuditEvent(Instant.now(), domain, context.clientId(), context.role(), action, verdict, detail));
    }
}
