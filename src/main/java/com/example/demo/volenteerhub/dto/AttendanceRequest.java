package com.example.demo.volenteerhub.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull Boolean attended,
        @NotNull BigDecimal hoursContributed) {
}
