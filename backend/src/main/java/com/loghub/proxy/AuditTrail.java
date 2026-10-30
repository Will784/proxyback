package com.loghub.proxy;

import com.loghub.dto.AuditEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Bounded, newest-first ring of proxy decisions (O(1) insert, memory capped). */
public final class AuditTrail {

    private final Deque<AuditEvent> events = new ArrayDeque<>();
    private final int capacity;

    public AuditTrail(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void record(AuditEvent event) {
        if (events.size() == capacity) {
            events.removeLast();
        }
        events.addFirst(event);
    }

    public synchronized List<AuditEvent> latest(int limit) {
        return events.stream().limit(Math.max(1, limit)).toList();
    }
}
