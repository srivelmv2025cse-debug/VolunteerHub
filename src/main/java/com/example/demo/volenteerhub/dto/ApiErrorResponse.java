package com.example.demo.volenteerhub.dto;

import java.time.Instant;

public record ApiErrorResponse(int status, String message, Instant timestamp) {
}
