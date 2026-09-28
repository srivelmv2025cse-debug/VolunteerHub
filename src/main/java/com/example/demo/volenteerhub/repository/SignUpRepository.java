package com.example.demo.volenteerhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.volenteerhub.entity.SignUp;

public interface SignUpRepository extends JpaRepository<SignUp, Long> {

    boolean existsByVolunteer_IdAndEvent_Id(Long volunteerId, Long eventId);

    long countByEvent_Id(Long eventId);

    List<SignUp> findByEvent_Id(Long eventId);

    List<SignUp> findByVolunteer_Id(Long volunteerId);

    List<SignUp> findByVolunteer_IdOrderByEvent_DateAsc(Long volunteerId);
}