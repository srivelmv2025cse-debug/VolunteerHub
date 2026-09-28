package com.example.demo.volenteerhub.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.volenteerhub.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);

    List<Event> findByDate(LocalDate date);
}