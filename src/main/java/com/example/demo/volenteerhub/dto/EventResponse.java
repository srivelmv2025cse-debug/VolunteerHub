package com.example.demo.volenteerhub.dto;

import java.time.LocalDate;

public record EventResponse(
        Long id,
        String name,
        String description,
        LocalDate date,
        String location,
        Integer volunteerCapacity) {
}
