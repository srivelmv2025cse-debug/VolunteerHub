package com.example.demo.volenteerhub.service;

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

import com.example.demo.volenteerhub.dto.SignUpRequest;
import com.example.demo.volenteerhub.entity.Event;
import com.example.demo.volenteerhub.entity.SignUp;
import com.example.demo.volenteerhub.entity.Volunteer;
import com.example.demo.volenteerhub.repository.EventRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private SignUpRepository signUpRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private SignUpService signUpService;

    private Event event;
    private Volunteer volunteer;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setVolunteerCapacity(2);

        volunteer = new Volunteer();
        volunteer.setId(2L);
    }

    @Test
    void rejectsDuplicateRegistrationBeforeSaving() {
        givenValidEventAndVolunteer();
        when(signUpRepository.existsByVolunteer_IdAndEvent_Id(2L, 1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> signUpService.createSignUp(new SignUpRequest(1L, 2L)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Volunteer is already registered for this event.", exception.getReason());
        verify(signUpRepository, never()).save(any(SignUp.class));
        verify(signUpRepository, never()).countByEvent_Id(1L);
    }

    @Test
    void rejectsSignupWhenEventIsAtCapacity() {
        event.setVolunteerCapacity(1);
        givenValidEventAndVolunteer();
        when(signUpRepository.existsByVolunteer_IdAndEvent_Id(2L, 1L)).thenReturn(false);
        when(signUpRepository.countByEvent_Id(1L)).thenReturn(1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> signUpService.createSignUp(new SignUpRequest(1L, 2L)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Event is already full. Volunteer cannot register.", exception.getReason());
        verify(signUpRepository, never()).save(any(SignUp.class));
    }

    @Test
    void rejectsInvalidEventIdBeforeCheckingVolunteer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> signUpService.createSignUp(new SignUpRequest(1L, 2L)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(volunteerRepository, never()).findById(2L);
        verify(signUpRepository, never()).save(any(SignUp.class));
    }

    @Test
    void rejectsInvalidVolunteerIdBeforeCheckingRegistration() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(volunteerRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> signUpService.createSignUp(new SignUpRequest(1L, 2L)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(signUpRepository, never()).existsByVolunteer_IdAndEvent_Id(2L, 1L);
        verify(signUpRepository, never()).save(any(SignUp.class));
    }

    @Test
    void savesSignupAfterAllChecksPass() {
        givenValidEventAndVolunteer();
        when(signUpRepository.existsByVolunteer_IdAndEvent_Id(2L, 1L)).thenReturn(false);
        when(signUpRepository.countByEvent_Id(1L)).thenReturn(1L);
        when(signUpRepository.save(any(SignUp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = signUpService.createSignUp(new SignUpRequest(1L, 2L));

        assertEquals(1L, response.eventId());
        assertEquals(2L, response.volunteerId());
        verify(signUpRepository).save(any(SignUp.class));
    }

    private void givenValidEventAndVolunteer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(volunteerRepository.findById(2L)).thenReturn(Optional.of(volunteer));
    }
}
