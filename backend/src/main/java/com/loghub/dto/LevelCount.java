package com.loghub.dto;

import com.loghub.domain.LogLevel;

public record LevelCount(LogLevel level, Long total) { }
