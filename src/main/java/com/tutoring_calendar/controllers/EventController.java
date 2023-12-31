package com.tutoring_calendar.controllers;

import com.tutoring_calendar.dto.EventResponse;
import com.tutoring_calendar.dto.EventUpdateDTO;
import com.tutoring_calendar.models.Event;
import com.tutoring_calendar.services.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@Slf4j
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents(){
        List<Event> events = eventService.getAllEvents();
        if(events.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{date}")
    public ResponseEntity<EventResponse> getEventsByWeek(@PathVariable LocalDate date) {
        log.debug("Received request to get events for week with date: {}", date);

        EventResponse eventResponse = eventService.getEventsForSelectedWeek(date);

        log.info("Successfully retrieved events and calculated incomes for the selected week.");
        return ResponseEntity.ok(eventResponse);
    }

    @PostMapping("/events/update")
    public ResponseEntity<Object> updateEvent(@RequestBody EventUpdateDTO newEvent){
        log.info("Received request to update event. Date details: {}", newEvent);

        Optional<Event> updatedEventOptional = eventService.updateEventData(newEvent);

        if(updatedEventOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Event updatedEvent = updatedEventOptional.get();
        Long id = updatedEvent.getId();

        log.info("Date updated successfully. Event ID: {}", id);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/events/create-event")
    public ResponseEntity<Object> createNewEvent(@RequestBody Event newEvent){
        log.info("Received request to create a new event. Date details: {}", newEvent);

        Optional<Event> createdEvent = eventService.addEvent(newEvent);

        return createdEvent.map(event -> {
            Long id = event.getId();
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();
            log.info("Date created successfully. Date ID: {}", id);
            return ResponseEntity.created(location).build();
        }).orElse(ResponseEntity.noContent().build());
    }

}
