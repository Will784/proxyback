package com.loghub.dto;

import com.loghub.domain.LogDomain;
import com.loghub.domain.LogLevel;

import java.time.Instant;
import java.util.Map;

public record ServiceStats(LogDomain domain, long total, Map<LogLevel, Long> byLevel, Instant computedAt) { }
