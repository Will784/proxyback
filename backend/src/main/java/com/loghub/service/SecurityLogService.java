package com.loghub.service;

import com.loghub.domain.LogDomain;
import com.loghub.domain.LogLevel;
import com.loghub.dto.LogRequest;
import com.loghub.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SecurityLogService extends AbstractLogService {

    private static final List<String> SUSPICIOUS_TERMS = List.of("failed", "denied", "unauthorized", "forbidden", "fallido", "denegado");

    public SecurityLogService(LogEntryRepository repository) {
        super(repository);
    }

    @Override
    protected LogDomain domain() {
        return LogDomain.SECURITY;
    }

    /** Authentication/authorisation failures are never stored below WARN. */
    @Override
    protected LogLevel resolveLevel(LogRequest request) {
        String text = request.message().toLowerCase(Locale.ROOT);
        boolean suspicious = SUSPICIOUS_TERMS.stream().anyMatch(text::contains);
        return suspicious && request.level().compareTo(LogLevel.WARN) < 0 ? LogLevel.WARN : request.level();
    }
}
