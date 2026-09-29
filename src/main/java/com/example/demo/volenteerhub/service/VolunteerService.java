package com.example.demo.volenteerhub.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.volenteerhub.dto.VolunteerHistoryResponse;
import com.example.demo.volenteerhub.dto.VolunteerHoursResponse;
import com.example.demo.volenteerhub.dto.VolunteerRequest;
import com.example.demo.volenteerhub.dto.VolunteerResponse;
import com.example.demo.volenteerhub.entity.AttendanceRecord;
import com.example.demo.volenteerhub.entity.SignUp;
import com.example.demo.volenteerhub.entity.Volunteer;
import com.example.demo.volenteerhub.exception.DuplicateVolunteerException;
import com.example.demo.volenteerhub.exception.ResourceNotFoundException;
import com.example.demo.volenteerhub.repository.AttendanceRecordRepository;
import com.example.demo.volenteerhub.repository.SignUpRepository;
import com.example.demo.volenteerhub.repository.VolunteerRepository;

@Service
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SignUpRepository signUpRepository;

    public VolunteerService(
            VolunteerRepository volunteerRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            SignUpRepository signUpRepository) {
        this.volunteerRepository = volunteerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.signUpRepository = signUpRepository;
    }

    public VolunteerResponse createVolunteer(VolunteerRequest request) {
        String email = normalizeEmail(request.email());
        if (volunteerRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateVolunteerException();
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
            throw new DuplicateVolunteerException();
        }

        volunteer.setName(request.name().trim());
        volunteer.setEmail(email);
        volunteer.setPhone(normalizePhone(request.phone()));
        return toResponse(volunteerRepository.save(volunteer));
    }

    public void deleteVolunteer(Long id) {
        volunteerRepository.delete(findVolunteer(id));
    }

    @Transactional(readOnly = true)
    public VolunteerHoursResponse getVolunteerHours(Long id) {
        Volunteer volunteer = findVolunteer(id);
        BigDecimal totalHours = attendanceRecordRepository
                .findBySignUp_Volunteer_IdAndAttendedTrue(id).stream()
                .filter(record -> record.isAttended()
                        && record.getHoursContributed() != null
                        && record.getHoursContributed().signum() >= 0)
                .map(AttendanceRecord::getHoursContributed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new VolunteerHoursResponse(volunteer.getId(), volunteer.getName(), totalHours);
    }

    @Transactional(readOnly = true)
    public List<VolunteerHistoryResponse> getVolunteerHistory(Long id) {
        findVolunteer(id);
        return signUpRepository.findByVolunteer_IdOrderByEvent_DateAsc(id).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private Volunteer findVolunteer(Long id) {
        return volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));
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

    private VolunteerHistoryResponse toHistoryResponse(SignUp signUp) {
        AttendanceRecord attendance = signUp.getAttendanceRecord();
        boolean attended = attendance != null && attendance.isAttended();
        BigDecimal hours = attended && attendance.getHoursContributed() != null
                ? attendance.getHoursContributed()
                : BigDecimal.ZERO;
        return new VolunteerHistoryResponse(
                signUp.getEvent().getName(),
                signUp.getEvent().getDate(),
                signUp.getEvent().getLocation(),
                attended,
                hours);
    }
}
