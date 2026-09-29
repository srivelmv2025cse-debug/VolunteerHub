package com.example.demo.volenteerhub.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.volenteerhub.entity.Event;
import com.example.demo.volenteerhub.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventSearchServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void filtersEventsByTrimmedLocation() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Campus Cleanup");
        event.setLocation("College Hall");
        when(eventRepository.findByLocationContainingIgnoreCase("College")).thenReturn(List.of(event));

        var result = eventService.getAllEvents(" College ");

        assertEquals(1, result.size());
        assertEquals("College Hall", result.get(0).location());
        verify(eventRepository).findByLocationContainingIgnoreCase("College");
    }
}
