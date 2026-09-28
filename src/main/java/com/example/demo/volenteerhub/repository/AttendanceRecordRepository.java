package com.example.demo.volenteerhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.volenteerhub.entity.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findBySignUp_Id(Long signupId);

    List<AttendanceRecord> findBySignUp_Event_Id(Long eventId);
}