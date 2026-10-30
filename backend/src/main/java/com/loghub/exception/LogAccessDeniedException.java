package com.loghub.exception;

import org.springframework.http.HttpStatus;

public class LogAccessDeniedException extends LogHubException {

    public LogAccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }
}
