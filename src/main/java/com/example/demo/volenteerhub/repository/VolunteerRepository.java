package com.example.demo.volenteerhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.volenteerhub.entity.Volunteer;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}