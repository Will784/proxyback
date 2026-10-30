package com.loghub.exception;

import org.springframework.http.HttpStatus;

public class DuplicateLogException extends LogHubException {

    public DuplicateLogException(String message) {
        super(HttpStatus.CONFLICT, "DUPLICATE_LOG", message);
    }
}
