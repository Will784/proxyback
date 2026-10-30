package com.loghub.service;

import com.loghub.domain.LogDomain;
import com.loghub.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsLogService extends AbstractLogService {

    public AnalyticsLogService(LogEntryRepository repository) {
        super(repository);
    }

    @Override
    protected LogDomain domain() {
        return LogDomain.ANALYTICS;
    }
}
