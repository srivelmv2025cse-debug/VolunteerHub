package com.example.demo.volenteerhub.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.volenteerhub.dto.SignUpRequest;
import com.example.demo.volenteerhub.dto.SignUpResponse;
import com.example.demo.volenteerhub.service.SignUpService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SignUpController {

    private final SignUpService signUpService;

    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    @PostMapping("/signups")
    public ResponseEntity<SignUpResponse> createSignUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse createdSignUp = signUpService.createSignUp(request);
        return ResponseEntity.created(URI.create("/api/signups/" + createdSignUp.id())).body(createdSignUp);
    }

    @GetMapping("/signups/{id}")
    public SignUpResponse getSignUpById(@PathVariable Long id) {
        return signUpService.getSignUpById(id);
    }

    @GetMapping("/events/{eventId}/signups")
    public List<SignUpResponse> getSignUpsForEvent(@PathVariable Long eventId) {
        return signUpService.getSignUpsForEvent(eventId);
    }

    @GetMapping("/volunteers/{volunteerId}/signups")
    public List<SignUpResponse> getSignUpsForVolunteer(@PathVariable Long volunteerId) {
        return signUpService.getSignUpsForVolunteer(volunteerId);
    }
}
