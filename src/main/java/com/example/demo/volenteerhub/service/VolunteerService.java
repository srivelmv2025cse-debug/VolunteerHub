package com.example.demo.volenteerhub.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.volenteerhub.dto.VolunteerRequest;
import com.example.demo.volenteerhub.dto.VolunteerResponse;
import com.example.demo.volenteerhub.entity.Volunteer;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@Service
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    public VolunteerResponse createVolunteer(VolunteerRequest request) {
        String email = normalizeEmail(request.email());
        if (volunteerRepository.existsByEmailIgnoreCase(email)) {
            throw duplicateEmailException();
        }

        Volunteer volunteer = new Volunteer();
        volunteer.setName(request.name().trim());
        volunteer.setEmail(email);
        volunteer.setPhone(normalizePhone(request.phone()));
        return toResponse(volunteerRepository.save(volunteer));
    }

    public List<VolunteerResponse> getAllVolunteers() {
        return volunteerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public VolunteerResponse getVolunteerById(Long id) {
        return toResponse(findVolunteer(id));
    }

    public VolunteerResponse updateVolunteer(Long id, VolunteerRequest request) {
        Volunteer volunteer = findVolunteer(id);
        String email = normalizeEmail(request.email());
        if (volunteerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw duplicateEmailException();
        }

        volunteer.setName(request.name().trim());
        volunteer.setEmail(email);
        volunteer.setPhone(normalizePhone(request.phone()));
        return toResponse(volunteerRepository.save(volunteer));
    }

    public void deleteVolunteer(Long id) {
        volunteerRepository.delete(findVolunteer(id));
    }

    private Volunteer findVolunteer(Long id) {
        return volunteerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));
    }

    private ResponseStatusException duplicateEmailException() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Volunteer with this email already exists.");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private VolunteerResponse toResponse(Volunteer volunteer) {
        return new VolunteerResponse(
                volunteer.getId(),
                volunteer.getName(),
                volunteer.getEmail(),
                volunteer.getPhone());
    }
}
