package com.example.demo.volenteerhub.dto;

import java.math.BigDecimal;

public record AttendanceResponse(
        Long id,
        Long signupId,
        boolean attended,
        BigDecimal hoursContributed) {
}
