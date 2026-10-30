package com.loghub.exception;

import org.springframework.http.HttpStatus;

public class LogNotFoundException extends LogHubException {

    public LogNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "LOG_NOT_FOUND", message);
    }
}
