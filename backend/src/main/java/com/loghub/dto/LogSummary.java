package com.loghub.dto;

import com.loghub.domain.LogLevel;

import java.time.Instant;

/** Light view of a log: the heavy payload is only reported by size, never loaded. */
public record LogSummary(Long id, LogLevel level, String source, String message,
                         Instant createdAt, Integer payloadSize) { }
