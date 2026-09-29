package com.example.demo.volenteerhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidAttendanceException extends ResponseStatusException {

    public InvalidAttendanceException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
