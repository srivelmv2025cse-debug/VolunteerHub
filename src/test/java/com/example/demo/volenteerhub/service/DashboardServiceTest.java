package com.example.demo.volenteerhub.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.volenteerhub.dto.DashboardSummaryResponse;
import com.example.demo.volenteerhub.repository.AttendanceRecordRepository;
import com.example.demo.volenteerhub.repository.EventRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private SignUpRepository signUpRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void returnsCountsAndDatabaseHoursAggregate() {
        when(eventRepository.count()).thenReturn(10L);
        when(volunteerRepository.count()).thenReturn(100L);
        when(signUpRepository.count()).thenReturn(250L);
        when(attendanceRecordRepository.count()).thenReturn(200L);
        when(attendanceRecordRepository.sumHoursForAttendedVolunteers()).thenReturn(new BigDecimal("850"));

        DashboardSummaryResponse response = dashboardService.getSummary();

        assertEquals(10L, response.totalEvents());
        assertEquals(100L, response.totalVolunteers());
        assertEquals(250L, response.totalSignups());
        assertEquals(200L, response.totalAttendanceRecords());
        assertEquals(new BigDecimal("850"), response.totalVolunteerHours());
        verify(attendanceRecordRepository).sumHoursForAttendedVolunteers();
    }
}
