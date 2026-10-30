package com.loghub.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Per-key token bucket: O(1) per decision, lock contention limited to the caller's own bucket.
 * Tokens refill continuously, so bursts up to {@code capacity} are allowed and the sustained rate is bounded.
 */
public final class TokenBucketRateLimiter {

    public record Decision(boolean allowed, long retryAfterMillis) { }

    private static final class Bucket {
        double tokens;
        volatile long lastRefillNanos;

        Bucket(double tokens, long lastRefillNanos) {
            this.tokens = tokens;
            this.lastRefillNanos = lastRefillNanos;
        }
    }

    private static final int PURGE_THRESHOLD = 10_000;
    private static final long IDLE_NANOS = 60_000_000_000L;

    private final int capacity;
    private final double refillPerNano;
    private final LongSupplier clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacity, double refillPerSecond) {
        this(capacity, refillPerSecond, System::nanoTime);
    }

    TokenBucketRateLimiter(int capacity, double refillPerSecond, LongSupplier clock) {
        if (capacity <= 0 || refillPerSecond <= 0) {
            throw new IllegalArgumentException("capacity and refill rate must be positive");
        }
        this.capacity = capacity;
        this.refillPerNano = refillPerSecond / 1_000_000_000.0;
        this.clock = clock;
    }

    public Decision tryAcquire(String key) {
        long now = clock.getAsLong();
        if (buckets.size() > PURGE_THRESHOLD) {
            buckets.entrySet().removeIf(e -> now - e.getValue().lastRefillNanos > IDLE_NANOS);
        }
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, now));
        synchronized (bucket) {
            bucket.tokens = Math.min(capacity, bucket.tokens + (now - bucket.lastRefillNanos) * refillPerNano);
            bucket.lastRefillNanos = now;
            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return new Decision(true, 0);
            }
            long waitMillis = (long) Math.ceil((1.0 - bucket.tokens) / refillPerNano / 1_000_000.0);
            return new Decision(false, waitMillis);
        }
    }
}
