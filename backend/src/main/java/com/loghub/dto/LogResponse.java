package com.loghub.dto;

import com.loghub.domain.LogDomain;
import com.loghub.domain.LogLevel;

import java.time.Instant;

public record LogResponse(Long id, LogDomain domain, LogLevel level, String source, String message,
                          String payload, String sourceIp, Instant createdAt) { }
