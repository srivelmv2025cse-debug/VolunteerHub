package com.example.demo.volenteerhub.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AttendanceRequest(
        @NotNull Boolean attended,
        @NotNull @PositiveOrZero BigDecimal hoursContributed) {
}
