package com.example.demo.volenteerhub.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
        @NotBlank String name,
        @Size(max = 2000) String description,
        @NotNull LocalDate date,
        @NotBlank String location,
        @NotNull @Positive Integer volunteerCapacity) {
}
