package com.example.demo.volenteerhub.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.volenteerhub.dto.SignUpRequest;
import com.example.demo.volenteerhub.dto.VolunteerRequest;
import com.example.demo.volenteerhub.exception.DuplicateSignupException;
import com.example.demo.volenteerhub.exception.DuplicateVolunteerException;
import com.example.demo.volenteerhub.exception.EventFullException;
import com.example.demo.volenteerhub.exception.InvalidAttendanceException;
import com.example.demo.volenteerhub.exception.ResourceNotFoundException;
import com.example.demo.volenteerhub.service.AttendanceService;
import com.example.demo.volenteerhub.service.SignUpService;
import com.example.demo.volenteerhub.service.VolunteerService;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private SignUpService signUpService;

        @Mock
        private VolunteerService volunteerService;

        @Mock
        private AttendanceService attendanceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new SignUpController(signUpService),
                        new VolunteerController(volunteerService),
                        new AttendanceController(attendanceService))
                .setControllerAdvice(new com.example.demo.volenteerhub.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void convertsValidationFailuresToClearBadRequestJson() throws Exception {
        mockMvc.perform(post("/api/signups")
                        .contentType("application/json")
                        .content("{\"eventId\":0,\"volunteerId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("eventId")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void mapsEventFullBusinessExceptionToConflictJson() throws Exception {
        when(signUpService.createSignUp(any(SignUpRequest.class))).thenThrow(new EventFullException());

        mockMvc.perform(post("/api/signups")
                        .contentType("application/json")
                        .content("{\"eventId\":1,\"volunteerId\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Event is already full. Volunteer cannot register."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void mapsDuplicateSignupBusinessExceptionToConflictJson() throws Exception {
        when(signUpService.createSignUp(any(SignUpRequest.class))).thenThrow(new DuplicateSignupException());

        mockMvc.perform(post("/api/signups")
                        .contentType("application/json")
                        .content("{\"eventId\":1,\"volunteerId\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Volunteer is already registered for this event."));
    }

    @Test
    void mapsDuplicateVolunteerToConflictJson() throws Exception {
        when(volunteerService.createVolunteer(any(VolunteerRequest.class)))
                .thenThrow(new DuplicateVolunteerException());

        mockMvc.perform(post("/api/volunteers")
                        .contentType("application/json")
                        .content("{\"name\":\"Avery\",\"email\":\"avery@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Volunteer with this email already exists."));
    }

    @Test
    void mapsMissingResourceToNotFoundJson() throws Exception {
        when(signUpService.getSignUpById(99L)).thenThrow(new ResourceNotFoundException("Sign-up not found"));

        mockMvc.perform(get("/api/signups/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Sign-up not found"));
    }

    @Test
    void mapsInvalidAttendanceToBadRequestJson() throws Exception {
        when(attendanceService.recordAttendance(eq(1L), any()))
                .thenThrow(new InvalidAttendanceException(
                        "Contribution hours can only be recorded for volunteers marked as present."));

        mockMvc.perform(put("/api/attendance/1")
                        .contentType("application/json")
                        .content("{\"attended\":false,\"hoursContributed\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Contribution hours can only be recorded for volunteers marked as present."));
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        when(signUpService.createSignUp(any(SignUpRequest.class)))
                .thenThrow(new IllegalStateException("internal secret detail"));

        mockMvc.perform(post("/api/signups")
                        .contentType("application/json")
                        .content("{\"eventId\":1,\"volunteerId\":2}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("internal secret detail"))));
    }
}
