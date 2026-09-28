package com.example.demo.volenteerhub.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.volenteerhub.dto.AttendanceRequest;
import com.example.demo.volenteerhub.dto.AttendanceResponse;
import com.example.demo.volenteerhub.entity.AttendanceRecord;
import com.example.demo.volenteerhub.entity.Event;
import com.example.demo.volenteerhub.entity.SignUp;
import com.example.demo.volenteerhub.repository.AttendanceRecordRepository;
import com.example.demo.volenteerhub.repository.EventRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private SignUpRepository signUpRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private SignUp signUp;

    @BeforeEach
    void setUp() {
        signUp = new SignUp();
        signUp.setId(10L);
        Event event = new Event();
        event.setId(1L);
        signUp.setEvent(event);
    }

    @Test
    void recordsPresentWithContributionHours() {
        givenSignupExists();
        when(attendanceRecordRepository.findBySignUp_Id(10L)).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.recordAttendance(
                10L, new AttendanceRequest(true, new BigDecimal("4")));

        assertEquals(10L, response.signupId());
        assertEquals(true, response.attended());
        assertEquals(new BigDecimal("4"), response.hoursContributed());
        verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    void recordsAbsentWithZeroHours() {
        givenSignupExists();
        when(attendanceRecordRepository.findBySignUp_Id(10L)).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.recordAttendance(
                10L, new AttendanceRequest(false, BigDecimal.ZERO));

        assertEquals(false, response.attended());
        assertEquals(BigDecimal.ZERO, response.hoursContributed());
        verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
    }

    @Test
    void rejectsAbsentWithPositiveHours() {
        givenSignupExists();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> attendanceService.recordAttendance(
                        10L, new AttendanceRequest(false, new BigDecimal("4"))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Contribution hours can only be recorded for volunteers marked as present.",
                exception.getReason());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void rejectsNegativeHours() {
        givenSignupExists();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> attendanceService.recordAttendance(
                        10L, new AttendanceRequest(true, new BigDecimal("-1"))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Contribution hours cannot be negative.", exception.getReason());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void rejectsInvalidSignupId() {
        when(signUpRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> attendanceService.recordAttendance(
                        10L, new AttendanceRequest(true, BigDecimal.ONE)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    void updatesExistingAttendanceRecord() {
        givenSignupExists();
        AttendanceRecord existing = new AttendanceRecord();
        existing.setId(5L);
        existing.setSignUp(signUp);
        existing.setAttended(false);
        existing.setHoursContributed(BigDecimal.ZERO);
        when(attendanceRecordRepository.findBySignUp_Id(10L)).thenReturn(Optional.of(existing));
        when(attendanceRecordRepository.save(existing)).thenReturn(existing);

        AttendanceResponse response = attendanceService.recordAttendance(
                10L, new AttendanceRequest(true, new BigDecimal("2.5")));

        assertEquals(5L, response.id());
        assertEquals(true, response.attended());
        assertEquals(new BigDecimal("2.5"), response.hoursContributed());
        verify(attendanceRecordRepository).save(existing);
    }

    private void givenSignupExists() {
        when(signUpRepository.findById(10L)).thenReturn(Optional.of(signUp));
    }
}
