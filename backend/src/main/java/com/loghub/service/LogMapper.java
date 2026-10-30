package com.loghub.service;

import com.loghub.domain.LogEntry;
import com.loghub.dto.LogResponse;

final class LogMapper {

    private LogMapper() { }

    static LogResponse toResponse(LogEntry e) {
        return new LogResponse(e.getId(), e.getDomain(), e.getLevel(), e.getSource(), e.getMessage(),
                e.getPayload(), e.getSourceIp(), e.getCreatedAt());
    }
}
