package com.example.demo.volenteerhub.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.volenteerhub.dto.AttendanceRequest;
import com.example.demo.volenteerhub.dto.AttendanceResponse;
import com.example.demo.volenteerhub.entity.AttendanceRecord;
import com.example.demo.volenteerhub.entity.SignUp;
import com.example.demo.volenteerhub.exception.InvalidAttendanceException;
import com.example.demo.volenteerhub.exception.ResourceNotFoundException;
import com.example.demo.volenteerhub.repository.AttendanceRecordRepository;
import com.example.demo.volenteerhub.repository.EventRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SignUpRepository signUpRepository;
    private final EventRepository eventRepository;

    public AttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            SignUpRepository signUpRepository,
            EventRepository eventRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.signUpRepository = signUpRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public AttendanceResponse recordAttendance(Long signupId, AttendanceRequest request) {
        SignUp signUp = signUpRepository.findById(signupId)
            .orElseThrow(() -> new ResourceNotFoundException("Sign-up not found"));

        BigDecimal hours = request.hoursContributed();
        if (hours.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAttendanceException("Contribution hours cannot be negative.");
        }
        if (!request.attended() && hours.compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidAttendanceException(
                    "Contribution hours can only be recorded for volunteers marked as present.");
        }

        AttendanceRecord attendance = attendanceRecordRepository.findBySignUp_Id(signupId)
                .orElseGet(AttendanceRecord::new);
        attendance.setSignUp(signUp);
        attendance.setAttended(request.attended());
        attendance.setHoursContributed(hours);
        return toResponse(attendanceRecordRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceForEvent(Long eventId) {
        eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return attendanceRecordRepository.findBySignUp_Event_Id(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AttendanceResponse toResponse(AttendanceRecord attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getSignUp().getId(),
                attendance.isAttended(),
                attendance.getHoursContributed());
    }
}
