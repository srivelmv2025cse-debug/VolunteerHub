package com.example.demo.volenteerhub.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.volenteerhub.dto.AttendanceRequest;
import com.example.demo.volenteerhub.dto.AttendanceResponse;
import com.example.demo.volenteerhub.service.AttendanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PutMapping("/attendance/{signupId}")
    public AttendanceResponse recordAttendance(
            @PathVariable Long signupId,
            @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.recordAttendance(signupId, request);
    }

    @GetMapping("/events/{eventId}/attendance")
    public List<AttendanceResponse> getAttendanceForEvent(@PathVariable Long eventId) {
        return attendanceService.getAttendanceForEvent(eventId);
    }
}
