package com.example.demo.volenteerhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DuplicateVolunteerException extends ResponseStatusException {

    public DuplicateVolunteerException() {
        super(HttpStatus.CONFLICT, "Volunteer with this email already exists.");
    }
}
