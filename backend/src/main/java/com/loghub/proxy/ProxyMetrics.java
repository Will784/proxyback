package com.loghub.proxy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/** Fixed set of counters declared up-front (stable order for the UI, lock-free increments). */
public final class ProxyMetrics {

    private final Map<String, LongAdder> counters = new LinkedHashMap<>();

    public ProxyMetrics(String... names) {
        for (String name : names) {
            counters.put(name, new LongAdder());
        }
    }

    public void increment(String name) {
        LongAdder counter = counters.get(name);
        if (counter == null) {
            throw new IllegalArgumentException("Unknown metric: " + name);
        }
        counter.increment();
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> view = new LinkedHashMap<>();
        counters.forEach((name, counter) -> view.put(name, counter.sum()));
        return view;
    }
}
