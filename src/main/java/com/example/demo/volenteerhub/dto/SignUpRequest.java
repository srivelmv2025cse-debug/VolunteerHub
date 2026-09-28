package com.example.demo.volenteerhub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SignUpRequest(
        @NotNull @Positive Long eventId,
        @NotNull @Positive Long volunteerId) {
}
