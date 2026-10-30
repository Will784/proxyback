package com.loghub.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** Base type: every business failure carries its own HTTP status, stable code and Spanish message. */
public abstract class LogHubException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected LogHubException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }

    public String code() { return code; }

    public Map<String, Object> details() { return Map.of(); }
}
