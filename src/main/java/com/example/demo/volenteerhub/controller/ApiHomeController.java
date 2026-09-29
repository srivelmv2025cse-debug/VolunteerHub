package com.example.demo.volenteerhub.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiHomeController {

    @GetMapping("/api")
    public Map<String, String> getApiIndex() {
        return Map.of(
                "message", "VolunteerHub REST API is running.",
                "dashboard", "/api/dashboard/summary",
                "events", "/api/events",
                "volunteers", "/api/volunteers",
                "signups", "/api/signups");
    }
}
