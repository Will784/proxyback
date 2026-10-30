package com.loghub.service;

import com.loghub.domain.AccessContext;
import com.loghub.domain.LogDomain;
import com.loghub.domain.LogEntry;
import com.loghub.domain.LogLevel;
import com.loghub.dto.LevelCount;
import com.loghub.dto.LogRequest;
import com.loghub.dto.LogResponse;
import com.loghub.dto.LogSummary;
import com.loghub.dto.ServiceStats;
import com.loghub.exception.LogNotFoundException;
import com.loghub.repository.LogEntryRepository;
import com.loghub.support.Hashing;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** RealSubject base: pure persistence logic, no access control, no throttling, no caching. */
public abstract class AbstractLogService implements LogService {

    private static final int MAX_PAGE_SIZE = 100;

    private final LogEntryRepository repository;

    protected AbstractLogService(LogEntryRepository repository) {
        this.repository = repository;
    }

    protected abstract LogDomain domain();

    /** Hook for domain-specific rules (e.g. level escalation). */
    protected LogLevel resolveLevel(LogRequest request) {
        return request.level();
    }

    @Override
    @Transactional
    public LogResponse save(LogRequest request, AccessContext context) {
        String fingerprint = Hashing.sha256(context.clientId(), request.source(),
                request.level().name(), request.message(), request.payload());
        LogEntry entry = new LogEntry(domain(), resolveLevel(request), request.source().trim(),
                request.message().trim(), request.payload(), request.sourceIp(), fingerprint, Instant.now());
        return LogMapper.toResponse(repository.save(entry));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogSummary> findRecent(int limit, AccessContext context) {
        int pageSize = Math.max(1, Math.min(limit, MAX_PAGE_SIZE));
        return repository.findSummaries(domain(), PageRequest.of(0, pageSize));
    }

    @Override
    @Transactional(readOnly = true)
    public LogResponse findById(Long id, AccessContext context) {
        return repository.findByIdAndDomain(id, domain())
                .map(LogMapper::toResponse)
                .orElseThrow(() -> new LogNotFoundException("No existe el log " + id + " en este servicio."));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStats stats(AccessContext context) {
        Map<LogLevel, Long> byLevel = new EnumMap<>(LogLevel.class);
        for (LogLevel level : LogLevel.values()) {
            byLevel.put(level, 0L);
        }
        for (LevelCount count : repository.countByLevel(domain())) {
            byLevel.put(count.level(), count.total());
        }
        long total = byLevel.values().stream().mapToLong(Long::longValue).sum();
        return new ServiceStats(domain(), total, byLevel, Instant.now());
    }
}
