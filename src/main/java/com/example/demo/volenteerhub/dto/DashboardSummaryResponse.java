package com.example.demo.volenteerhub.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalEvents,
        long totalVolunteers,
        long totalSignups,
        long totalAttendanceRecords,
        BigDecimal totalVolunteerHours) {
}
