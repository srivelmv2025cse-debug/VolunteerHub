package com.example.demo.volenteerhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EventFullException extends ResponseStatusException {

    public EventFullException() {
        super(HttpStatus.CONFLICT, "Event is already full. Volunteer cannot register.");
    }
}
