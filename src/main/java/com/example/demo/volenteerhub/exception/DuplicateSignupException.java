package com.example.demo.volenteerhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DuplicateSignupException extends ResponseStatusException {

    public DuplicateSignupException() {
        super(HttpStatus.CONFLICT, "Volunteer is already registered for this event.");
    }
}
