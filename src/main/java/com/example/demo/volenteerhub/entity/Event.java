package com.example.demo.volenteerhub.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer volunteerCapacity;

    @OneToMany(mappedBy = "event")
    private List<SignUp> signUps = new ArrayList<>();

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getVolunteerCapacity() {
        return volunteerCapacity;
    }

    public void setVolunteerCapacity(Integer volunteerCapacity) {
        this.volunteerCapacity = volunteerCapacity;
    }

    public List<SignUp> getSignUps() {
        return signUps;
    }

    public void setSignUps(List<SignUp> signUps) {
        this.signUps = signUps;
    }
}