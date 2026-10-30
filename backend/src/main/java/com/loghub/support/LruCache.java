package com.loghub.support;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

/**
 * Thread-safe LRU cache with per-entry TTL.
 * Access-ordered LinkedHashMap gives O(1) get/put/evict; expiry is checked lazily on read.
 */
public final class LruCache<K, V> {

    private record Entry<V>(V value, long expiresAtNanos) { }

    private final LinkedHashMap<K, Entry<V>> store;
    private final long ttlNanos;
    private final LongSupplier clock;

    public LruCache(int capacity, Duration ttl) {
        this(capacity, ttl, System::nanoTime);
    }

    LruCache(int capacity, Duration ttl, LongSupplier clock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.ttlNanos = ttl.toNanos();
        this.clock = clock;
        this.store = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, Entry<V>> eldest) {
                return size() > capacity;
            }
        };
    }

    public synchronized Optional<V> get(K key) {
        Entry<V> entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry)) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    public synchronized void put(K key, V value) {
        store.put(key, new Entry<>(value, clock.getAsLong() + ttlNanos));
    }

    /** Atomic "claim": returns true only when the key was absent (or expired) and is now stored. */
    public synchronized boolean putIfAbsent(K key, V value) {
        if (get(key).isPresent()) {
            return false;
        }
        put(key, value);
        return true;
    }

    public synchronized void invalidate(K key) {
        store.remove(key);
    }

    public synchronized void clear() {
        store.clear();
    }

    public synchronized int size() {
        return store.size();
    }

    private boolean isExpired(Entry<V> entry) {
        return entry.expiresAtNanos() - clock.getAsLong() <= 0;
    }
}
