package com.example.demo.volenteerhub.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.volenteerhub.dto.CreateEventRequest;
import com.example.demo.volenteerhub.dto.EventResponse;
import com.example.demo.volenteerhub.dto.UpdateEventRequest;
import com.example.demo.volenteerhub.entity.Event;
import com.example.demo.volenteerhub.exception.ResourceNotFoundException;
import com.example.demo.volenteerhub.repository.EventRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(CreateEventRequest request) {
        Event event = new Event();
        event.setName(request.name());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setVolunteerCapacity(request.volunteerCapacity());
        return toResponse(eventRepository.save(event));
    }

    public List<EventResponse> getAllEvents(String location) {
        List<Event> events = location == null || location.isBlank()
                ? eventRepository.findAll()
                : eventRepository.findByLocationContainingIgnoreCase(location.trim());
        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEventById(Long id) {
        return toResponse(findEvent(id));
    }

    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        Event event = findEvent(id);
        event.setName(request.name());
        event.setDescription(request.description());
        event.setDate(request.date());
        event.setLocation(request.location());
        event.setVolunteerCapacity(request.volunteerCapacity());
        return toResponse(eventRepository.save(event));
    }

    public void deleteEvent(Long id) {
        Event event = findEvent(id);
        eventRepository.delete(event);
    }

    public List<EventResponse> getUpcomingEvents() {
        return eventRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getDate(),
                event.getLocation(),
                event.getVolunteerCapacity());
    }
}
