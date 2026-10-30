package com.loghub.support;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LruCacheTest {

    @Test
    void evictsLeastRecentlyUsedEntry() {
        LruCache<String, Integer> cache = new LruCache<>(2, Duration.ofSeconds(10));
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");
        cache.put("c", 3);

        assertTrue(cache.get("b").isEmpty());
        assertEquals(1, cache.get("a").orElseThrow());
    }

    @Test
    void expiresEntriesAfterTtl() {
        AtomicLong now = new AtomicLong();
        LruCache<String, Integer> cache = new LruCache<>(2, Duration.ofSeconds(1), now::get);
        cache.put("a", 1);
        now.addAndGet(Duration.ofSeconds(2).toNanos());

        assertTrue(cache.get("a").isEmpty());
    }

    @Test
    void putIfAbsentClaimsKeyOnlyOnce() {
        LruCache<String, Boolean> cache = new LruCache<>(2, Duration.ofSeconds(10));
        assertTrue(cache.putIfAbsent("k", true));
        assertFalse(cache.putIfAbsent("k", true));
    }
}
