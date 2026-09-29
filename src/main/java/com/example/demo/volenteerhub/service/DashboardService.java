package com.example.demo.volenteerhub.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.volenteerhub.dto.DashboardSummaryResponse;
import com.example.demo.volenteerhub.repository.AttendanceRecordRepository;
import com.example.demo.volenteerhub.repository.EventRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@Service
public class DashboardService {

    private final EventRepository eventRepository;
    private final VolunteerRepository volunteerRepository;
    private final SignUpRepository signUpRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public DashboardService(
            EventRepository eventRepository,
            VolunteerRepository volunteerRepository,
            SignUpRepository signUpRepository,
            AttendanceRecordRepository attendanceRecordRepository) {
        this.eventRepository = eventRepository;
        this.volunteerRepository = volunteerRepository;
        this.signUpRepository = signUpRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalHours = attendanceRecordRepository.sumHoursForAttendedVolunteers();
        return new DashboardSummaryResponse(
                eventRepository.count(),
                volunteerRepository.count(),
                signUpRepository.count(),
                attendanceRecordRepository.count(),
                totalHours == null ? BigDecimal.ZERO : totalHours);
    }
}
