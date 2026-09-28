package com.example.demo.volenteerhub.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.volenteerhub.dto.VolunteerHistoryResponse;
import com.example.demo.volenteerhub.dto.VolunteerHoursResponse;
import com.example.demo.volenteerhub.entity.AttendanceRecord;
import com.example.demo.volenteerhub.entity.Event;
import com.example.demo.volenteerhub.entity.SignUp;
import com.example.demo.volenteerhub.entity.Volunteer;
import com.example.demo.volenteerhub.repository.AttendanceRecordRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@ExtendWith(MockitoExtension.class)
class VolunteerHistoryServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private SignUpRepository signUpRepository;

    @InjectMocks
    private VolunteerService volunteerService;

    @Test
    void totalsPresentHoursAcrossMultipleEvents() {
        Volunteer volunteer = volunteer();
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(attendanceRecordRepository.findBySignUp_Volunteer_IdAndAttendedTrue(1L)).thenReturn(List.of(
                attendance(true, "4"),
                attendance(true, "8"),
                attendance(false, "6")));

        VolunteerHoursResponse response = volunteerService.getVolunteerHours(1L);

        assertEquals(1L, response.volunteerId());
        assertEquals("Srivel MV", response.volunteerName());
        assertEquals(new BigDecimal("12"), response.totalHours());
    }

    @Test
    void returnsOrderedParticipationHistoryIncludingUnrecordedAttendance() {
        Volunteer volunteer = volunteer();
        SignUp earlier = signUp(10L, "Tree Plantation Drive", LocalDate.parse("2026-10-10"), "Park", true, "4");
        SignUp later = signUp(11L, "Food Donation Drive", LocalDate.parse("2026-10-15"), "Food Bank", true, "3");
        SignUp notYetRecorded = signUp(12L, "Community Cleanup", LocalDate.parse("2026-10-20"), "Town Square", false, null);
        when(volunteerRepository.findById(1L)).thenReturn(Optional.of(volunteer));
        when(signUpRepository.findByVolunteer_IdOrderByEvent_DateAsc(1L))
                .thenReturn(List.of(earlier, later, notYetRecorded));

        List<VolunteerHistoryResponse> history = volunteerService.getVolunteerHistory(1L);

        assertEquals(3, history.size());
        assertEquals("Tree Plantation Drive", history.get(0).eventName());
        assertEquals(new BigDecimal("4"), history.get(0).hours());
        assertEquals("Food Donation Drive", history.get(1).eventName());
        assertEquals(new BigDecimal("3"), history.get(1).hours());
        assertEquals("Community Cleanup", history.get(2).eventName());
        assertEquals(false, history.get(2).attended());
        assertEquals(BigDecimal.ZERO, history.get(2).hours());
    }

    @Test
    void rejectsUnknownVolunteerForHoursAndHistory() {
        when(volunteerRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException hoursException = assertThrows(
                ResponseStatusException.class,
                () -> volunteerService.getVolunteerHours(99L));
        ResponseStatusException historyException = assertThrows(
                ResponseStatusException.class,
                () -> volunteerService.getVolunteerHistory(99L));

        assertEquals(HttpStatus.NOT_FOUND, hoursException.getStatusCode());
        assertEquals("Volunteer not found", hoursException.getReason());
        assertEquals(HttpStatus.NOT_FOUND, historyException.getStatusCode());
        assertEquals("Volunteer not found", historyException.getReason());
    }

    private Volunteer volunteer() {
        Volunteer volunteer = new Volunteer();
        volunteer.setId(1L);
        volunteer.setName("Srivel MV");
        return volunteer;
    }

    private AttendanceRecord attendance(boolean attended, String hours) {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttended(attended);
        record.setHoursContributed(new BigDecimal(hours));
        return record;
    }

    private SignUp signUp(
            Long id,
            String eventName,
            LocalDate date,
            String location,
            boolean attended,
            String hours) {
        Event event = new Event();
        event.setName(eventName);
        event.setDate(date);
        event.setLocation(location);
        SignUp signUp = new SignUp();
        signUp.setId(id);
        signUp.setEvent(event);
        if (hours != null) {
            AttendanceRecord attendance = attendance(attended, hours);
            attendance.setSignUp(signUp);
            signUp.setAttendanceRecord(attendance);
        }
        return signUp;
    }
}
