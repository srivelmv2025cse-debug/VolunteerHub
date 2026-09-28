package com.example.demo.volenteerhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.example.demo.volenteerhub.dto.VolunteerRequest;
import com.example.demo.volenteerhub.entity.Volunteer;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@ExtendWith(MockitoExtension.class)
class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private VolunteerService volunteerService;

    @Test
    void createVolunteerRejectsDuplicateEmail() {
        VolunteerRequest request = new VolunteerRequest("Avery Smith", "avery@example.com", "+1 555-123-4567");
        when(volunteerRepository.existsByEmailIgnoreCase("avery@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> volunteerService.createVolunteer(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Volunteer with this email already exists.", exception.getReason());
        verify(volunteerRepository, never()).save(any(Volunteer.class));
    }
}
