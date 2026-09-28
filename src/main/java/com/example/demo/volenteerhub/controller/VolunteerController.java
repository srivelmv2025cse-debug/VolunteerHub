package com.example.demo.volenteerhub.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.volenteerhub.dto.VolunteerRequest;
import com.example.demo.volenteerhub.dto.VolunteerHistoryResponse;
import com.example.demo.volenteerhub.dto.VolunteerHoursResponse;
import com.example.demo.volenteerhub.dto.VolunteerResponse;
import com.example.demo.volenteerhub.service.VolunteerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping
    public ResponseEntity<VolunteerResponse> createVolunteer(@Valid @RequestBody VolunteerRequest request) {
        VolunteerResponse createdVolunteer = volunteerService.createVolunteer(request);
        return ResponseEntity.created(URI.create("/api/volunteers/" + createdVolunteer.id()))
                .body(createdVolunteer);
    }

    @GetMapping
    public List<VolunteerResponse> getAllVolunteers() {
        return volunteerService.getAllVolunteers();
    }

    @GetMapping("/{id}")
    public VolunteerResponse getVolunteerById(@PathVariable Long id) {
        return volunteerService.getVolunteerById(id);
    }

    @GetMapping("/{id}/hours")
    public VolunteerHoursResponse getVolunteerHours(@PathVariable Long id) {
        return volunteerService.getVolunteerHours(id);
    }

    @GetMapping("/{id}/history")
    public List<VolunteerHistoryResponse> getVolunteerHistory(@PathVariable Long id) {
        return volunteerService.getVolunteerHistory(id);
    }

    @PutMapping("/{id}")
    public VolunteerResponse updateVolunteer(
            @PathVariable Long id,
            @Valid @RequestBody VolunteerRequest request) {
        return volunteerService.updateVolunteer(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable Long id) {
        volunteerService.deleteVolunteer(id);
        return ResponseEntity.noContent().build();
    }
}
