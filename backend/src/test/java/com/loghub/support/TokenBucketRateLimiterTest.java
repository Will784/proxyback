package com.loghub.support;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketRateLimiterTest {

    @Test
    void allowsBurstThenBlocksAndRecovers() {
        AtomicLong now = new AtomicLong();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, now::get);

        assertTrue(limiter.tryAcquire("client").allowed());
        assertTrue(limiter.tryAcquire("client").allowed());

        TokenBucketRateLimiter.Decision blocked = limiter.tryAcquire("client");
        assertFalse(blocked.allowed());
        assertEquals(1000, blocked.retryAfterMillis());

        now.addAndGet(Duration.ofSeconds(1).toNanos());
        assertTrue(limiter.tryAcquire("client").allowed());
    }

    @Test
    void bucketsAreIndependentPerClient() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, () -> 0L);
        assertTrue(limiter.tryAcquire("a").allowed());
        assertTrue(limiter.tryAcquire("b").allowed());
        assertFalse(limiter.tryAcquire("a").allowed());
    }
}
