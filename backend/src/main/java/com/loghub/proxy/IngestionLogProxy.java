package com.loghub.proxy;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.domain.Verdict;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.exception.DuplicateLogException;
import com.loghub.exception.InvalidLogException;
import com.loghub.exception.RateLimitExceededException;
import com.loghub.service.LogService;
import com.loghub.support.Hashing;
import com.loghub.support.LruCache;
import com.loghub.support.TokenBucketRateLimiter;

import java.time.Duration;

/** Gatekeeper proxy: validation -> rate limiting (token bucket per client) -> duplicate suppression. */
public final class IngestionLogProxy extends AbstractLogProxy {

    private static final int DEDUP_CAPACITY = 5_000;

    private final TokenBucketRateLimiter limiter;
    private final LogValidator validator;
    private final LruCache<String, Boolean> recentFingerprints;

    public IngestionLogProxy(LogService delegate, AuditTrail auditTrail, TokenBucketRateLimiter limiter,
                             LogValidator validator, Duration duplicateWindow) {
        super(LogDomain.INGESTION, delegate,
                new ProxyMetrics("calls", "accepted", "rejected", "throttled", "duplicates"), auditTrail);
        this.limiter = limiter;
        this.validator = validator;
        this.recentFingerprints = new LruCache<>(DEDUP_CAPACITY, duplicateWindow);
    }

    @Override
    public String proxyKind() {
        return "Proxy de validación y límite de tasa";
    }

    @Override
    public String responsibility() {
        return "Valida cada log, limita ráfagas por cliente con un cubo de fichas y descarta repetidos recientes.";
    }

    @Override
    protected LogResponse onSave(LogRequest request, AccessContext context) {
        validate(request, context);
        enforceRateLimit(context);
        String fingerprint = claimFingerprint(request, context);
        try {
            LogResponse saved = delegate.save(request, context);
            metrics.increment("accepted");
            return saved;
        } catch (RuntimeException ex) {
            recentFingerprints.invalidate(fingerprint);   // a failed write must not poison the dedup window
            throw ex;
        }
    }

    private void validate(LogRequest request, AccessContext context) {
        try {
            validator.validate(request);
        } catch (InvalidLogException ex) {
            metrics.increment("rejected");
            audit(context, "INGERIR", Verdict.REJECTED, ex.getMessage());
            throw ex;
        }
    }

    private void enforceRateLimit(AccessContext context) {
        TokenBucketRateLimiter.Decision decision = limiter.tryAcquire(context.clientId());
        if (decision.allowed()) {
            return;
        }
        long retryAfterSeconds = Math.max(1, (long) Math.ceil(decision.retryAfterMillis() / 1000.0));
        metrics.increment("throttled");
        audit(context, "INGERIR", Verdict.THROTTLED, "Límite de tasa superado; reintenta en " + retryAfterSeconds + " s");
        throw new RateLimitExceededException(
                "Estás enviando logs demasiado rápido. Reintenta en " + retryAfterSeconds + " s.", retryAfterSeconds);
    }

    private String claimFingerprint(LogRequest request, AccessContext context) {
        String fingerprint = Hashing.sha256(context.clientId(), request.source(), request.level().name(),
                request.message(), request.payload());
        if (!recentFingerprints.putIfAbsent(fingerprint, Boolean.TRUE)) {
            metrics.increment("duplicates");
            audit(context, "INGERIR", Verdict.DUPLICATE, "Log idéntico recibido hace pocos segundos");
            throw new DuplicateLogException("Ya recibimos un log idéntico hace pocos segundos; se descartó.");
        }
        return fingerprint;
    }
}
