package com.loghub.exception;

import org.springframework.http.HttpStatus;

public class InvalidLogException extends LogHubException {

    public InvalidLogException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_LOG", message);
    }
}
