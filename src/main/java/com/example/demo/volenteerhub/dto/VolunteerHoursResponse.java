package com.example.demo.volenteerhub.dto;

import java.math.BigDecimal;

public record VolunteerHoursResponse(Long volunteerId, String volunteerName, BigDecimal totalHours) {
}
