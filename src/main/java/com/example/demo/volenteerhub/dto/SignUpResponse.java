package com.example.demo.volenteerhub.dto;

import java.time.LocalDate;

public record SignUpResponse(Long id, Long eventId, Long volunteerId, LocalDate signupDate) {
}
