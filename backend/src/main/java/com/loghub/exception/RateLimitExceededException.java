package com.loghub.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class RateLimitExceededException extends LogHubException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    @Override
    public Map<String, Object> details() {
        return Map.of("retryAfterSeconds", retryAfterSeconds);
    }
}
