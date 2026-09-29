package com.example.demo.volenteerhub.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.volenteerhub.dto.SignUpRequest;
import com.example.demo.volenteerhub.dto.SignUpResponse;
import com.example.demo.volenteerhub.entity.Event;
import com.example.demo.volenteerhub.entity.SignUp;
import com.example.demo.volenteerhub.entity.Volunteer;
import com.example.demo.volenteerhub.exception.DuplicateSignupException;
import com.example.demo.volenteerhub.exception.EventFullException;
import com.example.demo.volenteerhub.exception.ResourceNotFoundException;
import com.example.demo.volenteerhub.repository.EventRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@Service
public class SignUpService {

    private final SignUpRepository signUpRepository;
    private final EventRepository eventRepository;
    private final VolunteerRepository volunteerRepository;

    public SignUpService(
            SignUpRepository signUpRepository,
            EventRepository eventRepository,
            VolunteerRepository volunteerRepository) {
        this.signUpRepository = signUpRepository;
        this.eventRepository = eventRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional
    public SignUpResponse createSignUp(SignUpRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Volunteer volunteer = volunteerRepository.findById(request.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));

        if (signUpRepository.existsByVolunteer_IdAndEvent_Id(volunteer.getId(), event.getId())) {
            throw new DuplicateSignupException();
        }

        long currentSignUps = signUpRepository.countByEvent_Id(event.getId());
        if (currentSignUps >= event.getVolunteerCapacity()) {
                        throw new EventFullException();
        }

        SignUp signUp = new SignUp();
        signUp.setEvent(event);
        signUp.setVolunteer(volunteer);
        signUp.setSignupDate(LocalDate.now());
        return toResponse(signUpRepository.save(signUp));
    }

    @Transactional(readOnly = true)
    public SignUpResponse getSignUpById(Long id) {
        SignUp signUp = signUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sign-up not found"));
        return toResponse(signUp);
    }

    @Transactional(readOnly = true)
    public List<SignUpResponse> getSignUpsForEvent(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return signUpRepository.findByEvent_Id(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SignUpResponse> getSignUpsForVolunteer(Long volunteerId) {
        volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));
        return signUpRepository.findByVolunteer_Id(volunteerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SignUpResponse toResponse(SignUp signUp) {
        return new SignUpResponse(
                signUp.getId(),
                signUp.getEvent().getId(),
                signUp.getVolunteer().getId(),
                signUp.getSignupDate());
    }
}
