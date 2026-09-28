package com.example.demo.volenteerhub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VolunteerHistoryResponse(
        String eventName,
        LocalDate date,
        String location,
        boolean attended,
        BigDecimal hours) {
}
